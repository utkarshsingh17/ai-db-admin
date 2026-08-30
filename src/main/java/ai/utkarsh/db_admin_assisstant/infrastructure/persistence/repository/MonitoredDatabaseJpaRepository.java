package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.MonitoredDatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MonitoredDatabaseJpaRepository extends JpaRepository<MonitoredDatabaseEntity, UUID> {

    List<MonitoredDatabaseEntity> findByEnabledTrue();

    boolean existsByName(String name);
}
