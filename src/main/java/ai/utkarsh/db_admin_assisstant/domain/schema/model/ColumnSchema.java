package ai.utkarsh.db_admin_assisstant.domain.schema.model;

public record ColumnSchema(String name, String dataType, boolean nullable, boolean primaryKey) {
}
