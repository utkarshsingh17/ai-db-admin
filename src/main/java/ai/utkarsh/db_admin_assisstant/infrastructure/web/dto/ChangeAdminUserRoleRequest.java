package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeAdminUserRoleRequest(@NotBlank String role) {
}
