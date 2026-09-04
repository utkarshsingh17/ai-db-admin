package ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUserId;

import java.util.UUID;

public interface ChangeAdminUserRoleUseCase {

    AdminUser changeRole(AdminUserId id, AdminRole newRole, UUID actingAdminId);
}
