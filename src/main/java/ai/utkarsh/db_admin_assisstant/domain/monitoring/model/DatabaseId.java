package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import java.util.Objects;
import java.util.UUID;

public record DatabaseId(UUID value) {

    public DatabaseId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static DatabaseId generate() {
        return new DatabaseId(UUID.randomUUID());
    }

    public static DatabaseId of(String value) {
        return new DatabaseId(UUID.fromString(value));
    }
}
