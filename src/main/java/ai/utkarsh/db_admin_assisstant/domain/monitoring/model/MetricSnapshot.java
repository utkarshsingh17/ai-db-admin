package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import java.time.Instant;
import java.util.Objects;

/** One poll's worth of instance-level health metrics for a monitored database. */
public class MetricSnapshot {

    private final MetricSnapshotId id;
    private final DatabaseId databaseId;
    private final Integer activeConnections;
    private final Integer maxConnections;
    private final Double cacheHitRatio;
    private final Integer lockWaitCount;
    private final Instant capturedAt;

    private MetricSnapshot(MetricSnapshotId id, DatabaseId databaseId, Integer activeConnections,
            Integer maxConnections, Double cacheHitRatio, Integer lockWaitCount, Instant capturedAt) {
        this.id = id;
        this.databaseId = databaseId;
        this.activeConnections = activeConnections;
        this.maxConnections = maxConnections;
        this.cacheHitRatio = cacheHitRatio;
        this.lockWaitCount = lockWaitCount;
        this.capturedAt = capturedAt;
    }

    public static MetricSnapshot capture(DatabaseId databaseId, Integer activeConnections, Integer maxConnections,
            Double cacheHitRatio, Integer lockWaitCount, Instant capturedAt) {
        Objects.requireNonNull(databaseId, "databaseId must not be null");
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        return new MetricSnapshot(MetricSnapshotId.generate(), databaseId, activeConnections, maxConnections,
                cacheHitRatio, lockWaitCount, capturedAt);
    }

    public static MetricSnapshot reconstitute(MetricSnapshotId id, DatabaseId databaseId, Integer activeConnections,
            Integer maxConnections, Double cacheHitRatio, Integer lockWaitCount, Instant capturedAt) {
        return new MetricSnapshot(id, databaseId, activeConnections, maxConnections, cacheHitRatio, lockWaitCount,
                capturedAt);
    }

    public boolean isConnectionPoolNearCapacity() {
        return activeConnections != null && maxConnections != null && maxConnections > 0
                && (activeConnections / (double) maxConnections) >= 0.9;
    }

    public boolean isCacheHitRatioLow() {
        return cacheHitRatio != null && cacheHitRatio < 0.90;
    }

    public MetricSnapshotId getId() {
        return id;
    }

    public DatabaseId getDatabaseId() {
        return databaseId;
    }

    public Integer getActiveConnections() {
        return activeConnections;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public Double getCacheHitRatio() {
        return cacheHitRatio;
    }

    public Integer getLockWaitCount() {
        return lockWaitCount;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }
}
