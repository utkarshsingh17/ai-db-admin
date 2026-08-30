package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

import java.util.UUID;

public interface SetMonitoredDatabaseEnabledUseCase {

    /** Disabling stops the scheduler from polling this target on its next cycle (see {@code findAllEnabled}). */
    MonitoredDatabase setEnabled(DatabaseId id, boolean enabled, UUID adminUserId);
}
