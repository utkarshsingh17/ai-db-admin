package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in;

import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;

import java.util.UUID;

public interface ApplyAiQueryUseCase {

    AiQueryApplyResult apply(RecommendationId id, UUID adminUserId, boolean revealPii);

    /** {@code optimizationRecommendationId} is null unless the query was slow enough to trigger a
     * follow-up performance recommendation (see {@code RecommendationOrchestrationService
     * #applyAiQuery}). */
    record AiQueryApplyResult(PerformanceRecommendation recommendation, QueryResult result,
            RecommendationId optimizationRecommendationId) {
    }
}
