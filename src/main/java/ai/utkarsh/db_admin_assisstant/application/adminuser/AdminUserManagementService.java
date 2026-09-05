package ai.utkarsh.db_admin_assisstant.application.adminuser;

import ai.utkarsh.db_admin_assisstant.application.audit.AuditLogService;
import ai.utkarsh.db_admin_assisstant.application.audit.JsonPayload;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUserId;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUserNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.ChangeAdminUserRoleUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.CreateAdminUserUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.ListAdminUsersUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.RegisterAdminUserUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.SetAdminUserEnabledUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.out.AdminUserRepository;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService implements CreateAdminUserUseCase, ListAdminUsersUseCase,
        ChangeAdminUserRoleUseCase, SetAdminUserEnabledUseCase, RegisterAdminUserUseCase {

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AdminUser create(CreateAdminUserCommand command) {
        if (repository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("An admin user with email '" + command.email() + "' already exists");
        }
        AdminUserId createdByAdminId = command.createdByAdminId() != null ? new AdminUserId(command.createdByAdminId())
                : null;
        AdminUser user = AdminUser.create(command.email(), passwordEncoder.encode(command.rawPassword()),
                command.role(), createdByAdminId);
        AdminUser saved = repository.save(user);
        auditLogService.record("SYSTEM", AuditAction.ADMIN_USER_CREATED, "AdminUser",
                saved.getId().value().toString(),
                JsonPayload.of().put("email", saved.getEmail()).put("role", saved.getRole()).build(), null);
        return saved;
    }

    /**
     * Every self-registered account is its own independent DB_ADMIN — not sponsored by, or
     * subordinate to, any other admin. Combined with per-admin database ownership scoping, this is
     * what makes self-registration produce a genuinely separate workspace: a new signup starts with
     * zero visible databases and only ever sees what they themselves register. DB_VIEWER remains a
     * real role, but only ever assigned explicitly by an admin (via the Users page) for someone
     * that specific admin wants to grant read-only access to their own databases — never a
     * self-registration default.
     */
    @Override
    @Transactional
    public AdminUser register(String email, String rawPassword) {
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email '" + email + "' already exists");
        }
        AdminUser user = AdminUser.create(email, passwordEncoder.encode(rawPassword), AdminRole.DB_ADMIN, null);
        AdminUser saved = repository.save(user);
        auditLogService.record("SYSTEM", AuditAction.ADMIN_USER_CREATED, "AdminUser",
                saved.getId().value().toString(),
                JsonPayload.of().put("email", saved.getEmail()).put("role", saved.getRole())
                        .put("selfRegistered", true).build(),
                null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUser> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public AdminUser changeRole(AdminUserId id, AdminRole newRole, UUID actingAdminId) {
        AdminUser user = repository.findById(id).orElseThrow(() -> new AdminUserNotFoundException(id));
        if (user.getRole() == AdminRole.DB_ADMIN && newRole != AdminRole.DB_ADMIN) {
            requireNotLastAdmin(user);
        }
        user.changeRole(newRole);
        AdminUser saved = repository.save(user);
        auditLogService.record(actingAdminId.toString(), AuditAction.ADMIN_USER_ROLE_CHANGED, "AdminUser",
                saved.getId().value().toString(), JsonPayload.of().put("role", saved.getRole()).build(), null);
        return saved;
    }

    @Override
    @Transactional
    public AdminUser setEnabled(AdminUserId id, boolean enabled, UUID actingAdminId) {
        AdminUser user = repository.findById(id).orElseThrow(() -> new AdminUserNotFoundException(id));
        if (!enabled && user.getRole() == AdminRole.DB_ADMIN) {
            requireNotLastAdmin(user);
        }
        if (enabled) {
            user.enable();
        } else {
            user.disable();
        }
        AdminUser saved = repository.save(user);
        auditLogService.record(actingAdminId.toString(),
                enabled ? AuditAction.ADMIN_USER_ENABLED : AuditAction.ADMIN_USER_DISABLED, "AdminUser",
                saved.getId().value().toString(), JsonPayload.of().put("email", saved.getEmail()).build(), null);
        return saved;
    }

    /** Refuses to strip admin rights from the last remaining enabled DB_ADMIN — otherwise nobody
     * left could undo the change. */
    private void requireNotLastAdmin(AdminUser user) {
        if (repository.countByRoleAndEnabledTrue(AdminRole.DB_ADMIN) <= 1) {
            throw new IllegalStateException(
                    "Cannot remove admin rights from '" + user.getEmail() + "' — they are the last enabled admin");
        }
    }
}
