package ai.utkarsh.db_admin_assisstant.domain.masking.model;

import java.util.Objects;
import java.util.UUID;

public record SensitiveColumnId(UUID value) {

    public SensitiveColumnId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static SensitiveColumnId generate() {
        return new SensitiveColumnId(UUID.randomUUID());
    }

    public static SensitiveColumnId of(String value) {
        return new SensitiveColumnId(UUID.fromString(value));
    }
}
