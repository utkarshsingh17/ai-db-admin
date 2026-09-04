package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAdminUserRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String role) {
}
