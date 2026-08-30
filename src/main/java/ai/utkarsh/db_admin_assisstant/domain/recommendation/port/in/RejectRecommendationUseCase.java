package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;

import java.util.UUID;

public interface RejectRecommendationUseCase {

    PerformanceRecommendation reject(RecommendationId id, UUID adminUserId, String reason);
}
