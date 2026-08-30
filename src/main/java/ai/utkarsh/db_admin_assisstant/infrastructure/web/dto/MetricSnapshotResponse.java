package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshot;

import java.time.Instant;

public record MetricSnapshotResponse(Integer activeConnections, Integer maxConnections, Double cacheHitRatio,
        Integer lockWaitCount, Instant capturedAt) {

    public static MetricSnapshotResponse from(MetricSnapshot snapshot) {
        return new MetricSnapshotResponse(snapshot.getActiveConnections(), snapshot.getMaxConnections(),
                snapshot.getCacheHitRatio(), snapshot.getLockWaitCount(), snapshot.getCapturedAt());
    }
}
