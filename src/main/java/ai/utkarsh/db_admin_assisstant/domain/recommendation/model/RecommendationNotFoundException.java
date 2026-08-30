package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import ai.utkarsh.db_admin_assisstant.domain.shared.DomainException;

public class RecommendationNotFoundException extends DomainException {

    public RecommendationNotFoundException(RecommendationId id) {
        super("RECOMMENDATION_NOT_FOUND", "No recommendation found with id: " + id.value());
    }
}
