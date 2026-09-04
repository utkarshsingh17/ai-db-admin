package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitManualSqlRequest(
        @NotBlank String databaseId,
        @NotBlank String title,
        @NotBlank String explanation,
        @NotBlank String proposedSql,
        @NotBlank String riskLevel,
        String targetObject) {
}
