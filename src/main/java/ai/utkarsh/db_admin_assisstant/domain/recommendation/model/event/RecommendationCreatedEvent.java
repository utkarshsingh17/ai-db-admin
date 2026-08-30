package ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationType;

import java.time.Instant;

public record RecommendationCreatedEvent(RecommendationId recommendationId, DatabaseId databaseId,
        RecommendationType type, Instant occurredAt) {
}
