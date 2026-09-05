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

    long countByRoleAndEnabledTrue(AdminRole role);

    /** The founding admin — used to sponsor self-registered viewers (see RegisterAdminUserUseCase)
     * and to resolve which admin's databases a viewer without any other context can see. */
    Optional<AdminUser> findEarliestByRole(AdminRole role);
}
