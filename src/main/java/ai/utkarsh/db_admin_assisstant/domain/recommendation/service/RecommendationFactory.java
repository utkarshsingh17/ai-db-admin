package ai.utkarsh.db_admin_assisstant.domain.recommendation.service;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationType;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RiskLevel;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.AiRecommendationDraft;

/**
 * Anti-Corruption Layer (Factory pattern): the AI's structured output never becomes a domain
 * object directly. This is the single place that turns an {@link AiRecommendationDraft} into a
 * valid, submitted {@link PerformanceRecommendation} aggregate — validating the proposed SQL shape
 * ({@link Sql}'s constructor) and recomputing risk before the recommendation can exist at all.
 */
public final class RecommendationFactory {

    private final RecommendationRiskAssessor riskAssessor;

    public RecommendationFactory(RecommendationRiskAssessor riskAssessor) {
        this.riskAssessor = riskAssessor;
    }

    public PerformanceRecommendation createFromAiDraft(DatabaseId databaseId, SlowQueryEventId slowQueryEventId,
            AiRecommendationDraft draft) {
        RecommendationType type = RecommendationType.valueOf(draft.type());
        Sql sql = new Sql(draft.proposedSql());
        RiskLevel riskLevel = riskAssessor.assess(type, sql);

        PerformanceRecommendation recommendation = PerformanceRecommendation.draft(databaseId, slowQueryEventId,
                type, riskLevel, draft.title(), draft.explanation(), sql, draft.targetObject());
        recommendation.submitForApproval();
        return recommendation;
    }
}
