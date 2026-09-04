package ai.utkarsh.db_admin_assisstant.domain.schema.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.DatabaseSchema;

import java.util.UUID;

public interface IntrospectDatabaseSchemaUseCase {

    DatabaseSchema introspect(DatabaseId databaseId, UUID actingAdminId);
}
