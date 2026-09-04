package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(UUID id, String email, String role, boolean enabled, Instant createdAt) {

    public static AdminUserResponse from(AdminUser user) {
        return new AdminUserResponse(user.getId().value(), user.getEmail(), user.getRole().name(), user.isEnabled(),
                user.getCreatedAt());
    }
}
