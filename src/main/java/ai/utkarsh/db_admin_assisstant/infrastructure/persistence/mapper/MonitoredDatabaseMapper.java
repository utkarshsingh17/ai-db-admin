package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.MonitoredDatabaseEntity;

public final class MonitoredDatabaseMapper {

    private MonitoredDatabaseMapper() {
    }

    public static MonitoredDatabase toDomain(MonitoredDatabaseEntity entity) {
        return MonitoredDatabase.reconstitute(new DatabaseId(entity.getId()), entity.getName(), entity.getEngine(),
                entity.getJdbcUrl(), entity.getUsername(), entity.getPassword(), entity.isEnabled(),
                entity.getOwnerAdminId(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static void updateEntity(MonitoredDatabaseEntity entity, MonitoredDatabase domain) {
        entity.setId(domain.getId().value());
        entity.setName(domain.getName());
        entity.setEngine(domain.getEngine());
        entity.setJdbcUrl(domain.getJdbcUrl());
        entity.setUsername(domain.getUsername());
        entity.setPassword(domain.getPassword());
        entity.setEnabled(domain.isEnabled());
        entity.setOwnerAdminId(domain.getOwnerAdminId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
