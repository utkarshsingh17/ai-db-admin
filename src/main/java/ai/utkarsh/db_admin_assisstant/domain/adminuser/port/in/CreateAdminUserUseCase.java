package ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;

import java.util.UUID;

public interface CreateAdminUserUseCase {

    AdminUser create(CreateAdminUserCommand command);

    record CreateAdminUserCommand(String email, String rawPassword, AdminRole role, UUID createdByAdminId) {
    }
}
