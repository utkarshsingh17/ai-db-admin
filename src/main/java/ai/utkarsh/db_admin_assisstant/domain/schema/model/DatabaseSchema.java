package ai.utkarsh.db_admin_assisstant.domain.schema.model;

import java.util.List;

public record DatabaseSchema(List<TableSchema> tables, List<ForeignKeySchema> foreignKeys) {
}
