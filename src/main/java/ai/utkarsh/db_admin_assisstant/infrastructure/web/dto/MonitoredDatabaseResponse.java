package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

import java.time.Instant;
import java.util.UUID;

public record MonitoredDatabaseResponse(UUID id, String name, String engine, String jdbcUrl, String username,
        boolean enabled, Instant createdAt) {

    public static MonitoredDatabaseResponse from(MonitoredDatabase database) {
        return new MonitoredDatabaseResponse(database.getId().value(), database.getName(),
                database.getEngine().name(), database.getJdbcUrl(), database.getUsername(), database.isEnabled(),
                database.getCreatedAt());
    }
}
