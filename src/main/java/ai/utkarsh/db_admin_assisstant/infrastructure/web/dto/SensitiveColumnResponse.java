package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;

import java.time.Instant;
import java.util.UUID;

public record SensitiveColumnResponse(UUID id, String tableName, String columnName, Instant createdAt) {

    public static SensitiveColumnResponse from(SensitiveColumn column) {
        return new SensitiveColumnResponse(column.getId().value(), column.getTableName(), column.getColumnName(),
                column.getCreatedAt());
    }
}
