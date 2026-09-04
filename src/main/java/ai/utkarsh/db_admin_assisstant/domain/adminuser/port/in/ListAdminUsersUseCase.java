package ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;

import java.util.List;

public interface ListAdminUsersUseCase {

    List<AdminUser> listAll();
}
