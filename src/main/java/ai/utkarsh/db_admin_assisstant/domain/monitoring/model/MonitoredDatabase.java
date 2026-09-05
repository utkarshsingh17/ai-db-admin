package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for a registered database target. The password is held in plain form in memory —
 * encryption-at-rest is applied transparently by the persistence adapter, not by the domain model,
 * since the collector needs the real credential to open a JDBC connection.
 */
public class MonitoredDatabase {

    private final DatabaseId id;
    private String name;
    private final DatabaseEngine engine;
    private String jdbcUrl;
    private String username;
    private String password;
    private boolean enabled;
    /** Raw UUID, not AdminUserId — this context doesn't own the admin-user aggregate, it just
     * remembers which one registered this database (same convention as the UUID actor ids on
     * recommendation domain events). Drives per-admin/per-viewer visibility scoping. */
    private final UUID ownerAdminId;
    private final Instant createdAt;
    private Instant updatedAt;

    private MonitoredDatabase(DatabaseId id, String name, DatabaseEngine engine, String jdbcUrl, String username,
            String password, boolean enabled, UUID ownerAdminId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.engine = engine;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.ownerAdminId = ownerAdminId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MonitoredDatabase register(String name, DatabaseEngine engine, String jdbcUrl, String username,
            String password, UUID ownerAdminId) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(engine, "engine must not be null");
        Objects.requireNonNull(jdbcUrl, "jdbcUrl must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        Objects.requireNonNull(ownerAdminId, "ownerAdminId must not be null");
        Instant now = Instant.now();
        return new MonitoredDatabase(DatabaseId.generate(), name, engine, jdbcUrl, username, password, true,
                ownerAdminId, now, now);
    }

    public static MonitoredDatabase reconstitute(DatabaseId id, String name, DatabaseEngine engine, String jdbcUrl,
            String username, String password, boolean enabled, UUID ownerAdminId, Instant createdAt,
            Instant updatedAt) {
        return new MonitoredDatabase(id, name, engine, jdbcUrl, username, password, enabled, ownerAdminId, createdAt,
                updatedAt);
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public DatabaseId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DatabaseEngine getEngine() {
        return engine;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public UUID getOwnerAdminId() {
        return ownerAdminId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
