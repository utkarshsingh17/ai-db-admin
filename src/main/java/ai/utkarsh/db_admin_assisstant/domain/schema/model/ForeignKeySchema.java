package ai.utkarsh.db_admin_assisstant.domain.schema.model;

public record ForeignKeySchema(String fromTable, String fromColumn, String toTable, String toColumn) {
}
