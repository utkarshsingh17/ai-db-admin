package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import ai.utkarsh.db_admin_assisstant.domain.shared.DomainException;

public class InvalidRecommendationStateException extends DomainException {

    public InvalidRecommendationStateException(RecommendationId id, RecommendationStatus current,
            String attemptedAction) {
        super("INVALID_RECOMMENDATION_STATE",
                "Cannot %s recommendation %s while in status %s".formatted(attemptedAction, id.value(), current));
    }
}
