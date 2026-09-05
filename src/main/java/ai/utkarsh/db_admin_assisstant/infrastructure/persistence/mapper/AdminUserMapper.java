package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUserId;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AdminUserEntity;

public final class AdminUserMapper {

    private AdminUserMapper() {
    }

    public static AdminUser toDomain(AdminUserEntity entity) {
        AdminUserId createdByAdminId = entity.getCreatedByAdminId() != null
                ? new AdminUserId(entity.getCreatedByAdminId())
                : null;
        return AdminUser.reconstitute(new AdminUserId(entity.getId()), entity.getEmail(), entity.getPasswordHash(),
                AdminRole.valueOf(entity.getRole()), entity.isEnabled(), createdByAdminId, entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static void updateEntity(AdminUserEntity entity, AdminUser domain) {
        entity.setId(domain.getId().value());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setRole(domain.getRole().name());
        entity.setEnabled(domain.isEnabled());
        entity.setCreatedByAdminId(domain.getCreatedByAdminId() != null ? domain.getCreatedByAdminId().value() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
