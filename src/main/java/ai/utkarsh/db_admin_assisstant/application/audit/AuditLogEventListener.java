package ai.utkarsh.db_admin_assisstant.application.audit;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationAppliedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationApplyFailedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationApprovedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationCreatedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationRejectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observer: turns every recommendation lifecycle event into an immutable audit entry. Bound to
 * {@code AFTER_COMMIT} so a rolled-back transaction never produces a misleading audit record —
 * see [[transactional-patterns]] / [[domain-driven-design]].
 */
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private static final String ENTITY_TYPE = "PerformanceRecommendation";
    private static final String SYSTEM_ACTOR = "SYSTEM";

    private final AuditLogService auditLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationCreated(RecommendationCreatedEvent event) {
        String payload = JsonPayload.of()
                .put("databaseId", event.databaseId().value())
                .put("type", event.type())
                .build();
        auditLogService.record(SYSTEM_ACTOR, AuditAction.RECOMMENDATION_CREATED, ENTITY_TYPE,
                event.recommendationId().value().toString(), payload, null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationApproved(RecommendationApprovedEvent event) {
        auditLogService.record(event.adminUserId().toString(), AuditAction.RECOMMENDATION_APPROVED, ENTITY_TYPE,
                event.recommendationId().value().toString(), null, null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationRejected(RecommendationRejectedEvent event) {
        String payload = JsonPayload.of().put("reason", event.reason()).build();
        auditLogService.record(event.adminUserId().toString(), AuditAction.RECOMMENDATION_REJECTED, ENTITY_TYPE,
                event.recommendationId().value().toString(), payload, null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationApplied(RecommendationAppliedEvent event) {
        auditLogService.record(event.appliedByAdminUserId().toString(), AuditAction.RECOMMENDATION_APPLIED,
                ENTITY_TYPE, event.recommendationId().value().toString(), null, null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationApplyFailed(RecommendationApplyFailedEvent event) {
        String payload = JsonPayload.of().put("reason", event.reason()).build();
        auditLogService.record(event.appliedByAdminUserId().toString(), AuditAction.RECOMMENDATION_APPLY_FAILED,
                ENTITY_TYPE, event.recommendationId().value().toString(), payload, null);
    }
}
