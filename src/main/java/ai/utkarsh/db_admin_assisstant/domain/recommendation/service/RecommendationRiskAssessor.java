package ai.utkarsh.db_admin_assisstant.domain.recommendation.service;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationType;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RiskLevel;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;

import java.util.Locale;

/**
 * Recomputes risk deterministically instead of trusting the AI's self-reported assessment
 * ({@code AiRecommendationDraft#aiAssessedRisk} is informational only). Config/instance-wide
 * changes are always HIGH risk; index changes are MEDIUM (require CONCURRENTLY + downtime-free,
 * but still consume I/O and disk); anything else defaults to LOW.
 */
public final class RecommendationRiskAssessor {

    public RiskLevel assess(RecommendationType type, Sql sql) {
        String upper = sql.statement().toUpperCase(Locale.ROOT);
        if (type == RecommendationType.CONFIG_CHANGE || upper.startsWith("ALTER SYSTEM SET")) {
            return RiskLevel.HIGH;
        }
        if (type == RecommendationType.INDEX) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
