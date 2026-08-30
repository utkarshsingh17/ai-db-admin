package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import java.util.Objects;
import java.util.UUID;

public record SlowQueryEventId(UUID value) {

    public SlowQueryEventId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static SlowQueryEventId generate() {
        return new SlowQueryEventId(UUID.randomUUID());
    }

    public static SlowQueryEventId of(String value) {
        return new SlowQueryEventId(UUID.fromString(value));
    }
}
