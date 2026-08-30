package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDatabaseRequest(
        @NotBlank String name,
        @NotBlank String jdbcUrl,
        @NotBlank String username,
        @NotBlank String password) {
}
