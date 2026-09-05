package ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;

/**
 * Public self-registration — distinct from {@link CreateAdminUserUseCase}, which requires an
 * already-authenticated DB_ADMIN and lets them pick the new user's role explicitly. Every
 * self-registered account becomes its own independent DB_ADMIN (solving the bootstrap problem of a
 * fresh deployment with no admin yet, and giving every signup a genuinely separate workspace — see
 * per-admin database ownership scoping). DB_VIEWER is only ever assigned explicitly by an admin via
 * {@link CreateAdminUserUseCase} or the role-change endpoint, never as a self-registration default.
 */
public interface RegisterAdminUserUseCase {

    AdminUser register(String email, String rawPassword);
}
