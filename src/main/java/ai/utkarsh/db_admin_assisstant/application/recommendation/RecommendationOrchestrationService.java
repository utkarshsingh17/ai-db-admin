package ai.utkarsh.db_admin_assisstant.application.recommendation;

import ai.utkarsh.db_admin_assisstant.application.recommendation.command.CommandFactory;
import ai.utkarsh.db_admin_assisstant.application.recommendation.command.CommandInvoker;
import ai.utkarsh.db_admin_assisstant.application.recommendation.command.DatabaseChangeCommand;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.SlowQueryEventRepository;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApplyRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApproveRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ListRecommendationsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.RejectRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.RequestRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.AiRecommendationDraft;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.AiRecommendationPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.PerformanceRecommendationRepository;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.SlowQueryAnalysisInput;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.service.RecommendationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Facade: the single entry point the web layer, MCP tools, and the monitoring scheduler use for the
 * whole recommendation lifecycle. Every mutation persists, then publishes the domain events the
 * aggregate collected — {@code AuditLogEventListener} turns those into the audit trail after commit.
 */
@Service
@RequiredArgsConstructor
public class RecommendationOrchestrationService implements RequestRecommendationUseCase,
        ApproveRecommendationUseCase, RejectRecommendationUseCase, ApplyRecommendationUseCase,
        ListRecommendationsUseCase {

    private final PerformanceRecommendationRepository recommendationRepository;
    private final MonitoredDatabaseRepository monitoredDatabaseRepository;
    private final SlowQueryEventRepository slowQueryEventRepository;
    private final AiRecommendationPort aiRecommendationPort;
    private final RecommendationFactory recommendationFactory;
    private final CommandInvoker commandInvoker;
    private final RecommendationApplyCoordinator applyCoordinator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PerformanceRecommendation requestForSlowQuery(DatabaseId databaseId, SlowQueryEventId slowQueryEventId) {
        MonitoredDatabase database = monitoredDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown database: " + databaseId.value()));
        SlowQueryEvent event = slowQueryEventRepository.findById(slowQueryEventId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown slow query event: " + slowQueryEventId.value()));

        SlowQueryAnalysisInput input = new SlowQueryAnalysisInput(database.getName(), event.getNormalizedQuery(),
                event.getCalls(), event.getMeanExecTimeMs(), event.getTotalExecTimeMs());
        AiRecommendationDraft draft = aiRecommendationPort.draftRecommendation(input);

        PerformanceRecommendation recommendation = recommendationFactory.createFromAiDraft(databaseId,
                slowQueryEventId, draft);
        PerformanceRecommendation saved = recommendationRepository.save(recommendation);
        // Publish from the pre-save instance, not `saved` — the repository reconstitutes a brand
        // new domain object from the persisted entity, and that object never carries the events
        // collected on `recommendation` (reconstitute() intentionally starts with an empty event list).
        publish(recommendation);
        return saved;
    }

    @Override
    @Transactional
    public PerformanceRecommendation approve(RecommendationId id, UUID adminUserId, String comment) {
        PerformanceRecommendation recommendation = getById(id);
        recommendation.approve(adminUserId, comment);
        PerformanceRecommendation saved = recommendationRepository.save(recommendation);
        publish(recommendation);
        return saved;
    }

    @Override
    @Transactional
    public PerformanceRecommendation reject(RecommendationId id, UUID adminUserId, String reason) {
        PerformanceRecommendation recommendation = getById(id);
        recommendation.reject(adminUserId, reason);
        PerformanceRecommendation saved = recommendationRepository.save(recommendation);
        publish(recommendation);
        return saved;
    }

    /**
     * Deliberately NOT {@code @Transactional} at this level. The DDL call in the middle is a
     * synchronous external side effect against a database Spring doesn't manage — see
     * {@link RecommendationApplyCoordinator} for why running it inside a transaction that also
     * touches the same database can self-deadlock. Each half below commits independently.
     */
    @Override
    public PerformanceRecommendation apply(RecommendationId id, UUID adminUserId) {
        RecommendationApplyCoordinator.ApplyContext context = applyCoordinator.beginApplying(id);

        DatabaseChangeCommand command = CommandFactory.fromRecommendation(context.recommendation());
        DatabaseChangeExecutorPort.ExecutionResult result = commandInvoker.invoke(command, context.target());

        return applyCoordinator.completeApplying(id, adminUserId, result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceRecommendation> listByStatus(RecommendationStatus status, int limit) {
        return recommendationRepository.findByStatus(status, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceRecommendation getById(RecommendationId id) {
        return recommendationRepository.findById(id).orElseThrow(() -> new RecommendationNotFoundException(id));
    }

    private void publish(PerformanceRecommendation recommendation) {
        recommendation.pullDomainEvents().forEach(eventPublisher::publishEvent);
    }
}
