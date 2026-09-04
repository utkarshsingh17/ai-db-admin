package ai.utkarsh.db_admin_assisstant.application.recommendation;

import ai.utkarsh.db_admin_assisstant.application.audit.AuditLogService;
import ai.utkarsh.db_admin_assisstant.application.audit.JsonPayload;
import ai.utkarsh.db_admin_assisstant.application.masking.QueryResultMasker;
import ai.utkarsh.db_admin_assisstant.application.recommendation.command.CommandFactory;
import ai.utkarsh.db_admin_assisstant.application.recommendation.command.CommandInvoker;
import ai.utkarsh.db_admin_assisstant.application.recommendation.command.DatabaseChangeCommand;
import ai.utkarsh.db_admin_assisstant.application.shared.SqlStatementClassifier;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabaseNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.QueryFingerprint;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.SlowQueryEventRepository;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import ai.utkarsh.db_admin_assisstant.domain.query.port.out.ReadOnlyQueryExecutorPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationType;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApplyAiQueryUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApplyRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApproveRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.DraftOptimizationForQueryUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ListRecommendationsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.RejectRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.RequestRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.SubmitManualSqlUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.AiRecommendationDraft;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.AiRecommendationPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.PerformanceRecommendationRepository;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.SlowQueryAnalysisInput;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.service.RecommendationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Facade: the single entry point the web layer, MCP tools, and the monitoring scheduler use for the
 * whole recommendation lifecycle. Every mutation persists, then publishes the domain events the
 * aggregate collected — {@code AuditLogEventListener} turns those into the audit trail after commit.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationOrchestrationService implements RequestRecommendationUseCase,
        ApproveRecommendationUseCase, RejectRecommendationUseCase, ApplyRecommendationUseCase,
        ListRecommendationsUseCase, SubmitManualSqlUseCase, ApplyAiQueryUseCase, DraftOptimizationForQueryUseCase {

    private final PerformanceRecommendationRepository recommendationRepository;
    private final MonitoredDatabaseRepository monitoredDatabaseRepository;
    private final SlowQueryEventRepository slowQueryEventRepository;
    private final AiRecommendationPort aiRecommendationPort;
    private final RecommendationFactory recommendationFactory;
    private final CommandInvoker commandInvoker;
    private final RecommendationApplyCoordinator applyCoordinator;
    private final ReadOnlyQueryExecutorPort readOnlyQueryExecutorPort;
    private final QueryResultMasker queryResultMasker;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    @Value("${app.monitoring.slow-query-threshold-ms}")
    private double slowQueryThresholdMs;

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

    /**
     * Manually-submitted counterpart to {@link #requestForSlowQuery}: skips the AI drafting path
     * entirely (no {@link RecommendationFactory} involved — that's an Anti-Corruption Layer
     * specifically for AI output) and constructs the aggregate directly. The domain events the
     * aggregate collects are drained but deliberately *not* published — {@code AuditLogEventListener}
     * hardcodes the "SYSTEM" actor for AI-drafted creations, which would misattribute a manual
     * submission, so the audit entry is written directly here with the real submitting admin.
     */
    @Override
    @Transactional
    public PerformanceRecommendation submitManualSql(SubmitManualSqlCommand command) {
        MonitoredDatabase database = monitoredDatabaseRepository.findById(command.databaseId())
                .orElseThrow(() -> new MonitoredDatabaseNotFoundException(command.databaseId()));
        SqlStatementClassifier.requireWrite(command.proposedSql());
        SqlStatementClassifier.requireSingleStatement(command.proposedSql());

        PerformanceRecommendation recommendation = PerformanceRecommendation.draft(database.getId(), null,
                RecommendationType.MANUAL_SQL, command.riskLevel(), command.title(), command.explanation(),
                new Sql(command.proposedSql()), command.targetObject());
        recommendation.submitForApproval();
        recommendation.pullDomainEvents();

        PerformanceRecommendation saved = recommendationRepository.save(recommendation);
        auditLogService.record(command.submittedByAdminId().toString(), AuditAction.RECOMMENDATION_CREATED,
                "PerformanceRecommendation", saved.getId().value().toString(),
                JsonPayload.of().put("databaseId", database.getId().value()).put("type", "MANUAL_SQL").build(),
                null);
        return saved;
    }

    /**
     * Executes an approved {@code AI_QUERY} recommendation's SELECT and returns the (masked) result
     * — the counterpart to the DDL {@link #apply} above, but for a query that returns data instead
     * of a boolean success/failure. Deliberately not routed through {@link CommandFactory}/{@link
     * RecommendationApplyCoordinator}: those exist to guard {@code CREATE INDEX CONCURRENTLY}'s
     * self-deadlock risk, which a plain {@code SELECT} against a *different* JDBC connection never
     * has, so one ordinary transaction is enough here.
     */
    @Override
    @Transactional
    public AiQueryApplyResult apply(RecommendationId id, UUID adminUserId, boolean revealPii) {
        PerformanceRecommendation recommendation = getById(id);
        if (recommendation.getType() != RecommendationType.AI_QUERY) {
            throw new IllegalArgumentException("Recommendation " + id.value() + " is not an AI_QUERY recommendation");
        }
        MonitoredDatabase target = monitoredDatabaseRepository.findById(recommendation.getDatabaseId())
                .orElseThrow(() -> new MonitoredDatabaseNotFoundException(recommendation.getDatabaseId()));

        recommendation.startApplying();
        recommendationRepository.save(recommendation);

        QueryResult rawResult = readOnlyQueryExecutorPort.execute(target, recommendation.getProposedSql().statement());
        QueryResult maskedResult = queryResultMasker.mask(recommendation.getDatabaseId(), rawResult, revealPii);

        recommendation.markApplied(adminUserId);
        PerformanceRecommendation saved = recommendationRepository.save(recommendation);
        publish(recommendation);

        RecommendationId optimizationId = draftIfSlow(target, recommendation.getProposedSql().statement(),
                rawResult);
        return new AiQueryApplyResult(saved, maskedResult, optimizationId);
    }

    /**
     * Best-effort follow-up for a query actually run through the portal (this AI_QUERY apply path, or
     * the SQL editor's manual "Write SQL" execution via {@code QueryExecutionService}): if it was
     * slow, record it as a {@link SlowQueryEvent} (so it shows up in the existing Slow Queries list —
     * the only remaining producer of these now that background polling is gone) and reuse the exact
     * AI-drafting pipeline built for {@link #requestForSlowQuery} to draft an independent optimization
     * recommendation. AI drafting failures (including "no AI provider configured") must never fail
     * the query execution itself — the query already ran and its results are already on their way
     * back to the caller — but the captured event is saved regardless, so slowness is visible even
     * when drafting fails.
     */
    @Override
    @Transactional
    public RecommendationId draftIfSlow(MonitoredDatabase target, String sql, QueryResult result) {
        if (result.executionTimeMs() < slowQueryThresholdMs) {
            return null;
        }
        SlowQueryEvent event = SlowQueryEvent.capture(target.getId(), fingerprintOf(sql), sql, 1,
                result.executionTimeMs(), result.executionTimeMs(), (long) result.rowCount(), Instant.now());
        SlowQueryEvent savedEvent = slowQueryEventRepository.save(event);

        try {
            SlowQueryAnalysisInput input = new SlowQueryAnalysisInput(target.getName(), sql, 1,
                    result.executionTimeMs(), result.executionTimeMs());
            AiRecommendationDraft draft = aiRecommendationPort.draftRecommendation(input);
            PerformanceRecommendation optimization = recommendationFactory.createFromAiDraft(target.getId(),
                    savedEvent.getId(), draft);
            PerformanceRecommendation saved = recommendationRepository.save(optimization);
            publish(optimization);
            return saved.getId();
        } catch (RuntimeException e) {
            log.warn("Optimization recommendation drafting skipped for ad hoc query on {}: {}", target.getName(),
                    e.getMessage());
            return null;
        }
    }

    /** No {@code pg_stat_statements} queryid available for an ad hoc portal query, so the query text
     * itself (whitespace-normalized) stands in as the fingerprint — good enough to tell distinct
     * slow queries apart in the list, which is all this identity is used for. */
    private QueryFingerprint fingerprintOf(String sql) {
        String normalized = sql.strip().replaceAll("\\s+", " ");
        return new QueryFingerprint(Integer.toHexString(normalized.hashCode()));
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
