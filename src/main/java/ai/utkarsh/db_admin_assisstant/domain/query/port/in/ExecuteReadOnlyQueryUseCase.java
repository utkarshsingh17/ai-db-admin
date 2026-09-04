package ai.utkarsh.db_admin_assisstant.domain.query.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;

import java.util.UUID;

public interface ExecuteReadOnlyQueryUseCase {

    QueryResult execute(DatabaseId databaseId, String sql, UUID actingAdminId, boolean revealPii);
}
