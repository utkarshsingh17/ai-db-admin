package ai.utkarsh.db_admin_assisstant.domain.masking.port.out;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumnId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;

import java.util.List;
import java.util.Optional;

public interface SensitiveColumnRepository {

    SensitiveColumn save(SensitiveColumn column);

    Optional<SensitiveColumn> findById(SensitiveColumnId id);

    List<SensitiveColumn> findByDatabaseId(DatabaseId databaseId);

    boolean existsByDatabaseIdAndTableNameAndColumnName(DatabaseId databaseId, String tableName, String columnName);

    void deleteById(SensitiveColumnId id);
}
