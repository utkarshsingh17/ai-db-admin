package ai.utkarsh.db_admin_assisstant.domain.adminuser.model;

import ai.utkarsh.db_admin_assisstant.domain.shared.DomainException;

public class AdminUserNotFoundException extends DomainException {

    public AdminUserNotFoundException(AdminUserId id) {
        super("ADMIN_USER_NOT_FOUND", "No admin user found with id: " + id.value());
    }
}
