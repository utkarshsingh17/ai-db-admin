package ai.utkarsh.db_admin_assisstant.domain.query.model;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;

/** Result of asking the AI to draft a query — it never executes here. {@code sql}/
 * {@code recommendationId} are null when the model declines to answer; {@code explanation} always
 * carries why (either what the query does, or why it couldn't be drafted). */
public record AiQuerySubmission(String sql, String explanation, RecommendationId recommendationId) {
}
