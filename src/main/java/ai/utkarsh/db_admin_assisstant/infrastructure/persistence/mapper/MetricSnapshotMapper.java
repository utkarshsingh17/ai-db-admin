package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshot;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshotId;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.MetricSnapshotEntity;

import java.time.Instant;

public final class MetricSnapshotMapper {

    private MetricSnapshotMapper() {
    }

    public static MetricSnapshot toDomain(MetricSnapshotEntity entity) {
        return MetricSnapshot.reconstitute(new MetricSnapshotId(entity.getId()),
                new DatabaseId(entity.getDatabaseId()), entity.getActiveConnections(), entity.getMaxConnections(),
                entity.getCacheHitRatio(), entity.getLockWaitCount(), entity.getCapturedAt());
    }

    public static MetricSnapshotEntity toEntity(MetricSnapshot domain) {
        MetricSnapshotEntity entity = new MetricSnapshotEntity();
        entity.setId(domain.getId().value());
        entity.setDatabaseId(domain.getDatabaseId().value());
        entity.setActiveConnections(domain.getActiveConnections());
        entity.setMaxConnections(domain.getMaxConnections());
        entity.setCacheHitRatio(domain.getCacheHitRatio());
        entity.setLockWaitCount(domain.getLockWaitCount());
        entity.setCapturedAt(domain.getCapturedAt());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
