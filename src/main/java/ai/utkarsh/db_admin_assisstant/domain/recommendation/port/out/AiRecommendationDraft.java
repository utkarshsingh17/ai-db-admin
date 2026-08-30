package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out;

/**
 * The AI's structured-output response — never used as a domain object directly. See
 * {@code RecommendationFactory}, which is the Anti-Corruption Layer that turns this into a
 * {@code PerformanceRecommendation} aggregate after validating and recomputing risk.
 */
public record AiRecommendationDraft(
        String type, // "INDEX" | "CONFIG_CHANGE" | "QUERY_REWRITE"
        String title,
        String explanation,
        String proposedSql,
        String targetObject,
        String aiAssessedRisk // informational only — never trusted directly
) {
}
