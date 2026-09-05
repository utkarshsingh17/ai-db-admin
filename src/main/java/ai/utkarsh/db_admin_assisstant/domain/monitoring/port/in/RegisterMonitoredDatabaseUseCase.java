package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseEngine;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

import java.util.UUID;

public interface RegisterMonitoredDatabaseUseCase {

    MonitoredDatabase register(RegisterDatabaseCommand command);

    record RegisterDatabaseCommand(String name, DatabaseEngine engine, String jdbcUrl, String username,
            String password, UUID registeredByAdminId) {
    }
}
