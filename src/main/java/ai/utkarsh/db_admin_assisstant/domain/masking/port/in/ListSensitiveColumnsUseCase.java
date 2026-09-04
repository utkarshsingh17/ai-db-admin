package ai.utkarsh.db_admin_assisstant.domain.masking.port.in;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;

import java.util.List;

public interface ListSensitiveColumnsUseCase {

    List<SensitiveColumn> listForDatabase(DatabaseId databaseId);
}
