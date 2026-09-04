package ai.utkarsh.db_admin_assisstant.domain.adminuser.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root for an admin-panel user account. Distinct from Spring Security's own read of the
 * same {@code admin_user} table (see {@code AdminUserDetailsService}) — this aggregate models the
 * user-management use cases (create, list, change role, enable/disable), not authentication.
 */
public class AdminUser {

    private final AdminUserId id;
    private final String email;
    private String passwordHash;
    private AdminRole role;
    private boolean enabled;
    private final Instant createdAt;
    private Instant updatedAt;

    private AdminUser(AdminUserId id, String email, String passwordHash, AdminRole role, boolean enabled,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AdminUser create(String email, String passwordHash, AdminRole role) {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Instant now = Instant.now();
        return new AdminUser(AdminUserId.generate(), email, passwordHash, role, true, now, now);
    }

    public static AdminUser reconstitute(AdminUserId id, String email, String passwordHash, AdminRole role,
            boolean enabled, Instant createdAt, Instant updatedAt) {
        return new AdminUser(id, email, passwordHash, role, enabled, createdAt, updatedAt);
    }

    public void changeRole(AdminRole newRole) {
        Objects.requireNonNull(newRole, "newRole must not be null");
        this.role = newRole;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public AdminUserId getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AdminRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
