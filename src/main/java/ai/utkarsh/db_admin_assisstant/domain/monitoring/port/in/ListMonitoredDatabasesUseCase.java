package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

import java.util.List;
import java.util.UUID;

public interface ListMonitoredDatabasesUseCase {

    /** Unscoped — internal/system use only (e.g. the dev demo-data seeder checking whether it's
     * already registered something). Never expose this over an authenticated endpoint; use
     * {@link #listVisibleTo} there instead. */
    List<MonitoredDatabase> listAll();

    /** An admin sees only databases they personally registered; a viewer sees whatever databases
     * the admin who created their account registered. */
    List<MonitoredDatabase> listVisibleTo(UUID currentAdminUserId);
}
