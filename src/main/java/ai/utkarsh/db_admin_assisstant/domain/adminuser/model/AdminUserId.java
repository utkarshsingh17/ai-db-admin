package ai.utkarsh.db_admin_assisstant.domain.adminuser.model;

import java.util.Objects;
import java.util.UUID;

public record AdminUserId(UUID value) {

    public AdminUserId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AdminUserId generate() {
        return new AdminUserId(UUID.randomUUID());
    }

    public static AdminUserId of(String value) {
        return new AdminUserId(UUID.fromString(value));
    }
}
