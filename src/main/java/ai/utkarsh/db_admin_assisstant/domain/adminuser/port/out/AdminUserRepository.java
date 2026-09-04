package ai.utkarsh.db_admin_assisstant.domain.adminuser.port.out;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUserId;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository {

    AdminUser save(AdminUser user);

    Optional<AdminUser> findById(AdminUserId id);

    Optional<AdminUser> findByEmail(String email);

    List<AdminUser> findAll();

    boolean existsByEmail(String email);

    /** Whether any admin user exists at all — used to decide whether a self-registration is the
     * very first account (and so becomes DB_ADMIN) or a later one (DB_VIEWER by default). */
    boolean existsAny();

    long countByRoleAndEnabledTrue(AdminRole role);
}
