package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;

import java.util.List;
import java.util.Optional;

public interface PerformanceRecommendationRepository {

    PerformanceRecommendation save(PerformanceRecommendation recommendation);

    Optional<PerformanceRecommendation> findById(RecommendationId id);

    List<PerformanceRecommendation> findByStatus(RecommendationStatus status, int limit);

    List<PerformanceRecommendation> findByDatabase(DatabaseId databaseId, int limit);
}
