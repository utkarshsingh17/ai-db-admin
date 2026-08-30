package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.ApprovalDecision;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.ApprovalDecisionEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.PerformanceRecommendationEntity;

import java.util.List;

public final class PerformanceRecommendationMapper {

    private PerformanceRecommendationMapper() {
    }

    public static PerformanceRecommendation toDomain(PerformanceRecommendationEntity entity) {
        List<ApprovalDecision> decisions = entity.getApprovalDecisions().stream()
                .map(d -> ApprovalDecision.reconstitute(d.getId(), d.getDecision(), d.getAdminUserId(),
                        d.getComment(), d.getDecidedAt()))
                .toList();

        return PerformanceRecommendation.reconstitute(new RecommendationId(entity.getId()),
                new DatabaseId(entity.getDatabaseId()),
                entity.getSlowQueryEventId() == null ? null : new SlowQueryEventId(entity.getSlowQueryEventId()),
                entity.getType(), entity.getStatus(), entity.getRiskLevel(), entity.getTitle(),
                entity.getExplanation(), new Sql(entity.getProposedSql()), entity.getTargetObject(),
                entity.getFailureReason(), entity.getAppliedAt(), decisions, entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /** Applies domain state onto a (possibly newly loaded, possibly managed) entity, in place. */
    public static void updateEntity(PerformanceRecommendationEntity entity, PerformanceRecommendation domain) {
        entity.setId(domain.getId().value());
        entity.setDatabaseId(domain.getDatabaseId().value());
        entity.setSlowQueryEventId(
                domain.getSlowQueryEventId() == null ? null : domain.getSlowQueryEventId().value());
        entity.setType(domain.getType());
        entity.setStatus(domain.getStatus());
        entity.setRiskLevel(domain.getRiskLevel());
        entity.setTitle(domain.getTitle());
        entity.setExplanation(domain.getExplanation());
        entity.setProposedSql(domain.getProposedSql().statement());
        entity.setTargetObject(domain.getTargetObject());
        entity.setFailureReason(domain.getFailureReason());
        entity.setAppliedAt(domain.getAppliedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        // Approval decisions are append-only (the state machine allows at most one approve/reject
        // decision per recommendation today) — only the newly added tail needs to be materialized.
        List<ApprovalDecision> domainDecisions = domain.getApprovalDecisions();
        List<ApprovalDecisionEntity> existing = entity.getApprovalDecisions();
        if (domainDecisions.size() > existing.size()) {
            for (ApprovalDecision decision : domainDecisions.subList(existing.size(), domainDecisions.size())) {
                ApprovalDecisionEntity decisionEntity = new ApprovalDecisionEntity();
                decisionEntity.setId(decision.getId());
                decisionEntity.setRecommendation(entity);
                decisionEntity.setDecision(decision.getDecision());
                decisionEntity.setAdminUserId(decision.getAdminUserId());
                decisionEntity.setComment(decision.getComment());
                decisionEntity.setDecidedAt(decision.getDecidedAt());
                existing.add(decisionEntity);
            }
        }
    }
}
