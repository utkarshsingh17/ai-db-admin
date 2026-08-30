package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;

import java.time.Instant;
import java.util.UUID;

public record RecommendationResponse(UUID id, UUID databaseId, String type, String status, String riskLevel,
        String title, String explanation, String proposedSql, String targetObject, String failureReason,
        Instant appliedAt, Instant createdAt) {

    public static RecommendationResponse from(PerformanceRecommendation recommendation) {
        return new RecommendationResponse(recommendation.getId().value(), recommendation.getDatabaseId().value(),
                recommendation.getType().name(), recommendation.getStatus().name(),
                recommendation.getRiskLevel().name(), recommendation.getTitle(), recommendation.getExplanation(),
                recommendation.getProposedSql().statement(), recommendation.getTargetObject(),
                recommendation.getFailureReason(), recommendation.getAppliedAt(), recommendation.getCreatedAt());
    }
}
