package ai.utkarsh.db_admin_assisstant.domain.schema.model;

import java.util.List;

public record TableSchema(String name, List<ColumnSchema> columns) {
}
