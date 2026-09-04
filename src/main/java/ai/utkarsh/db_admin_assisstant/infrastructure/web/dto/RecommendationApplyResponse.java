package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;

/** {@code queryResult}/{@code optimizationRecommendationId} are null for the ordinary DDL apply
 * path — only populated when applying an {@code AI_QUERY} recommendation. */
public record RecommendationApplyResponse(RecommendationResponse recommendation, QueryResultResponse queryResult,
        String optimizationRecommendationId) {

    public static RecommendationApplyResponse of(PerformanceRecommendation recommendation) {
        return new RecommendationApplyResponse(RecommendationResponse.from(recommendation), null, null);
    }

    public static RecommendationApplyResponse ofQuery(PerformanceRecommendation recommendation, QueryResult result,
            RecommendationId optimizationRecommendationId) {
        return new RecommendationApplyResponse(RecommendationResponse.from(recommendation),
                QueryResultResponse.from(result),
                optimizationRecommendationId == null ? null : optimizationRecommendationId.value().toString());
    }
}
