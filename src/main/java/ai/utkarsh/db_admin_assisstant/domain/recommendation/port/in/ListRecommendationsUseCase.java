package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;

import java.util.List;

public interface ListRecommendationsUseCase {

    List<PerformanceRecommendation> listByStatus(RecommendationStatus status, int limit);

    PerformanceRecommendation getById(RecommendationId id);
}
