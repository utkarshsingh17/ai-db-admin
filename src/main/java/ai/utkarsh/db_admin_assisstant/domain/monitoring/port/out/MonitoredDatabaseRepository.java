package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitoredDatabaseRepository {

    MonitoredDatabase save(MonitoredDatabase database);

    Optional<MonitoredDatabase> findById(DatabaseId id);

    List<MonitoredDatabase> findAllEnabled();

    List<MonitoredDatabase> findAll();

    List<MonitoredDatabase> findByOwnerAdminId(UUID ownerAdminId);

    boolean existsByName(String name);

    void deleteById(DatabaseId id);
}
