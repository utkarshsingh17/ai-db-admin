package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

import java.util.List;

public interface ListMonitoredDatabasesUseCase {

    List<MonitoredDatabase> listAll();
}
