package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record QueryRequest(@NotBlank String sql) {
}
