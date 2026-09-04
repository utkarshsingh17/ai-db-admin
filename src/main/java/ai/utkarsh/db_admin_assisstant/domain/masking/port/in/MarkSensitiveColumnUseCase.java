package ai.utkarsh.db_admin_assisstant.domain.masking.port.in;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;

import java.util.UUID;

public interface MarkSensitiveColumnUseCase {

    SensitiveColumn mark(MarkSensitiveColumnCommand command);

    record MarkSensitiveColumnCommand(DatabaseId databaseId, String tableName, String columnName,
            UUID actingAdminId) {
    }
}
