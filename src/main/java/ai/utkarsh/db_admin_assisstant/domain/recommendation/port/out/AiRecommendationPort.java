package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out;

public interface AiRecommendationPort {

    AiRecommendationDraft draftRecommendation(SlowQueryAnalysisInput input);
}
