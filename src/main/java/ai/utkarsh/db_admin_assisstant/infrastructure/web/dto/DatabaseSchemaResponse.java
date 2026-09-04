package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.schema.model.ColumnSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.DatabaseSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.ForeignKeySchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.TableSchema;

import java.util.List;

public record DatabaseSchemaResponse(List<TableSchemaResponse> tables, List<ForeignKeySchemaResponse> foreignKeys) {

    public static DatabaseSchemaResponse from(DatabaseSchema schema) {
        return new DatabaseSchemaResponse(schema.tables().stream().map(TableSchemaResponse::from).toList(),
                schema.foreignKeys().stream().map(ForeignKeySchemaResponse::from).toList());
    }

    public record TableSchemaResponse(String name, List<ColumnSchemaResponse> columns) {
        public static TableSchemaResponse from(TableSchema table) {
            return new TableSchemaResponse(table.name(),
                    table.columns().stream().map(ColumnSchemaResponse::from).toList());
        }
    }

    public record ColumnSchemaResponse(String name, String dataType, boolean nullable, boolean primaryKey) {
        public static ColumnSchemaResponse from(ColumnSchema column) {
            return new ColumnSchemaResponse(column.name(), column.dataType(), column.nullable(),
                    column.primaryKey());
        }
    }

    public record ForeignKeySchemaResponse(String fromTable, String fromColumn, String toTable, String toColumn) {
        public static ForeignKeySchemaResponse from(ForeignKeySchema fk) {
            return new ForeignKeySchemaResponse(fk.fromTable(), fk.fromColumn(), fk.toTable(), fk.toColumn());
        }
    }
}
