package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.SensitiveColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SensitiveColumnJpaRepository extends JpaRepository<SensitiveColumnEntity, UUID> {

    List<SensitiveColumnEntity> findByDatabaseId(UUID databaseId);

    boolean existsByDatabaseIdAndTableNameAndColumnName(UUID databaseId, String tableName, String columnName);
}
