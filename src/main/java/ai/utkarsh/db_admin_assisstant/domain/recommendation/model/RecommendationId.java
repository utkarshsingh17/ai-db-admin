package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import java.util.Objects;
import java.util.UUID;

public record RecommendationId(UUID value) {

    public RecommendationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static RecommendationId generate() {
        return new RecommendationId(UUID.randomUUID());
    }

    public static RecommendationId of(String value) {
        return new RecommendationId(UUID.fromString(value));
    }
}
