package ai.utkarsh.db_admin_assisstant.application.recommendation;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.PerformanceRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Splits the "apply" step into two short, independently-committed transactions with the actual DDL
 * execution running OUTSIDE either one — the same "side effect after commit" discipline as
 * [[transactional-patterns]], applied to a JDBC call instead of an event listener.
 *
 * <p>Running the DDL execution <em>inside</em> the same transaction that read {@code monitored_database}
 * is actively dangerous, not just unclean: {@code CREATE INDEX CONCURRENTLY} on Postgres must wait
 * for every transaction that was already open when it started to finish before it can complete. If
 * the orchestrating call holds its own transaction open around the (synchronous) DDL call — which it
 * did in the original implementation — and the monitored target happens to be reachable from the same
 * Postgres instance as the app's own database (as it is for anyone monitoring their own app DB, or in
 * any close-by deployment), the two block each other forever. Kept as a separate bean, not private
 * methods on {@link RecommendationOrchestrationService}, so {@code @Transactional} actually applies —
 * self-invocation bypasses the Spring proxy.
 */
@Component
@RequiredArgsConstructor
class RecommendationApplyCoordinator {

    private final PerformanceRecommendationRepository recommendationRepository;
    private final MonitoredDatabaseRepository monitoredDatabaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    record ApplyContext(PerformanceRecommendation recommendation, MonitoredDatabase target) {
    }

    @Transactional
    ApplyContext beginApplying(RecommendationId id) {
        PerformanceRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new RecommendationNotFoundException(id));
        MonitoredDatabase target = monitoredDatabaseRepository.findById(recommendation.getDatabaseId())
                .orElseThrow(() -> new IllegalStateException("Target database no longer exists"));

        recommendation.startApplying();
        PerformanceRecommendation saved = recommendationRepository.save(recommendation);
        return new ApplyContext(saved, target);
    }

    @Transactional
    PerformanceRecommendation completeApplying(RecommendationId id, UUID adminUserId,
            DatabaseChangeExecutorPort.ExecutionResult result) {
        PerformanceRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new RecommendationNotFoundException(id));

        switch (result.outcome()) {
            case SUCCESS -> recommendation.markApplied(adminUserId);
            case ALREADY_EXISTS -> recommendation.markAlreadyExists(adminUserId);
            case FAILURE -> recommendation.markFailed(adminUserId, result.message());
        }
        PerformanceRecommendation saved = recommendationRepository.save(recommendation);
        // Pull events from `recommendation` (the mutated instance), not `saved` — see the identical
        // note in RecommendationOrchestrationService; the repository's save() reconstitutes a fresh
        // domain object from the persisted entity with an empty event list.
        recommendation.pullDomainEvents().forEach(eventPublisher::publishEvent);
        return saved;
    }
}
