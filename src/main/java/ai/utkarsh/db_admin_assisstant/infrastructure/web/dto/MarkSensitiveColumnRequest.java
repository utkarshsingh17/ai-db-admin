package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkSensitiveColumnRequest(@NotBlank String tableName, @NotBlank String columnName) {
}
