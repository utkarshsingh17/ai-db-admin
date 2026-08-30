package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.QueryFingerprint;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.SlowQueryEventEntity;

import java.time.Instant;

public final class SlowQueryEventMapper {

    private SlowQueryEventMapper() {
    }

    public static SlowQueryEvent toDomain(SlowQueryEventEntity entity) {
        return SlowQueryEvent.reconstitute(new SlowQueryEventId(entity.getId()),
                new DatabaseId(entity.getDatabaseId()), new QueryFingerprint(entity.getQueryFingerprint()),
                entity.getNormalizedQuery(), entity.getCalls(), entity.getMeanExecTimeMs(),
                entity.getTotalExecTimeMs(), entity.getRowsReturned(), entity.getCapturedAt());
    }

    public static SlowQueryEventEntity toEntity(SlowQueryEvent domain) {
        SlowQueryEventEntity entity = new SlowQueryEventEntity();
        entity.setId(domain.getId().value());
        entity.setDatabaseId(domain.getDatabaseId().value());
        entity.setQueryFingerprint(domain.getFingerprint().value());
        entity.setNormalizedQuery(domain.getNormalizedQuery());
        entity.setCalls(domain.getCalls());
        entity.setMeanExecTimeMs(domain.getMeanExecTimeMs());
        entity.setTotalExecTimeMs(domain.getTotalExecTimeMs());
        entity.setRowsReturned(domain.getRowsReturned());
        entity.setCapturedAt(domain.getCapturedAt());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
