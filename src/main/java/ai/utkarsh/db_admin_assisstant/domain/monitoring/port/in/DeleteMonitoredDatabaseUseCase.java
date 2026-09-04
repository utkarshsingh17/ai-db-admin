package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;

import java.util.UUID;

public interface DeleteMonitoredDatabaseUseCase {

    void delete(DatabaseId id, UUID adminUserId);
}
