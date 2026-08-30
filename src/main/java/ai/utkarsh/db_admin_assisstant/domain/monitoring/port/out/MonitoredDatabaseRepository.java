package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

import java.util.List;
import java.util.Optional;

public interface MonitoredDatabaseRepository {

    MonitoredDatabase save(MonitoredDatabase database);

    Optional<MonitoredDatabase> findById(DatabaseId id);

    List<MonitoredDatabase> findAllEnabled();

    List<MonitoredDatabase> findAll();

    boolean existsByName(String name);
}
