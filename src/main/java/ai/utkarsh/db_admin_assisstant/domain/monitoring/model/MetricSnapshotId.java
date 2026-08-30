package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import java.util.Objects;
import java.util.UUID;

public record MetricSnapshotId(UUID value) {

    public MetricSnapshotId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MetricSnapshotId generate() {
        return new MetricSnapshotId(UUID.randomUUID());
    }

    public static MetricSnapshotId of(String value) {
        return new MetricSnapshotId(UUID.fromString(value));
    }
}
