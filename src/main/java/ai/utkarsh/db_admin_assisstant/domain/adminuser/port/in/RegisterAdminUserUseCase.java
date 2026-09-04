package ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;

/**
 * Public self-registration — distinct from {@link CreateAdminUserUseCase}, which requires an
 * already-authenticated DB_ADMIN and lets them pick the new user's role explicitly. Here the role
 * is decided automatically: the very first account ever created becomes DB_ADMIN (solving the
 * bootstrap problem of a fresh deployment with no admin yet), every account after that defaults to
 * DB_VIEWER — an admin can promote it via the existing role-change endpoint if needed.
 */
public interface RegisterAdminUserUseCase {

    AdminUser register(String email, String rawPassword);
}
