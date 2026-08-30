package ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;

import java.time.Instant;
import java.util.UUID;

public record RecommendationApplyFailedEvent(RecommendationId recommendationId, UUID appliedByAdminUserId,
        String reason, Instant occurredAt) {
}
