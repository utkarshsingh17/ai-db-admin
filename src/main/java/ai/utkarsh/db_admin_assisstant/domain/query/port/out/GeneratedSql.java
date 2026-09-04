package ai.utkarsh.db_admin_assisstant.domain.query.port.out;

/**
 * The AI's structured-output response for a natural-language-to-SQL request — never used as a
 * domain object directly, same posture as {@code AiRecommendationDraft}. {@code sql} is blank when
 * the model declines to answer.
 */
public record GeneratedSql(String sql, String explanation) {
}
