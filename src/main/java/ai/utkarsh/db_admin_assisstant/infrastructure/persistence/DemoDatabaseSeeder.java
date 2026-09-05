package ai.utkarsh.db_admin_assisstant.infrastructure.persistence;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.out.AdminUserRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseEngine;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.ListMonitoredDatabasesUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.RegisterMonitoredDatabaseUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Optional;

/**
 * Dev-only: creates the ride-hailing demo database (a separate physical Postgres database on the
 * same server as the app's own datasource, not this app's own schema), loads its schema + seed data,
 * and registers it as a monitored database — so a fresh clone has something realistic to point the
 * SQL editor / AI assistant / PII masking at without any manual setup. Every step is guarded so
 * re-running on an already-seeded environment is a no-op.
 */
@Component
@Profile("dev")
@Order(2)
@Slf4j
public class DemoDatabaseSeeder implements ApplicationRunner {

    private static final String DEMO_DATABASE_NAME = "admin_assistant";
    private static final String MONITORED_DATABASE_NAME = "admin-assistant-demo";
    private static final String SEED_SCRIPT_PATH = "db/seed/ride-hailing-demo.sql";
    private static final String SENTINEL_TABLE = "company";

    private final RegisterMonitoredDatabaseUseCase registerUseCase;
    private final ListMonitoredDatabasesUseCase listUseCase;
    private final AdminUserRepository adminUserRepository;
    private final String adminJdbcUrl;
    private final String adminUsername;
    private final String adminPassword;

    public DemoDatabaseSeeder(RegisterMonitoredDatabaseUseCase registerUseCase,
            ListMonitoredDatabasesUseCase listUseCase, AdminUserRepository adminUserRepository,
            @Value("${spring.datasource.url}") String adminJdbcUrl,
            @Value("${spring.datasource.username}") String adminUsername,
            @Value("${spring.datasource.password}") String adminPassword) {
        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
        this.adminUserRepository = adminUserRepository;
        this.adminJdbcUrl = adminJdbcUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        String demoJdbcUrl = withDatabaseName(adminJdbcUrl, DEMO_DATABASE_NAME);
        try {
            ensureDemoDatabaseExists();
            ensureSchemaAndDataSeeded(demoJdbcUrl);
            ensureRegisteredAsMonitoredDatabase(demoJdbcUrl);
        } catch (SQLException | IOException e) {
            log.warn("Ride-hailing demo database seeding skipped: {}", e.getMessage());
        }
    }

    private void ensureDemoDatabaseExists() throws SQLException {
        try (Connection connection = DriverManager.getConnection(adminJdbcUrl, adminUsername, adminPassword)) {
            try (PreparedStatement ps = connection
                    .prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                ps.setString(1, DEMO_DATABASE_NAME);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return;
                    }
                }
            }
            // CREATE DATABASE cannot run inside a transaction block; a fresh DriverManager
            // connection defaults to autocommit=true, same requirement as CREATE INDEX CONCURRENTLY
            // elsewhere in this codebase.
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE " + DEMO_DATABASE_NAME);
            }
            log.info("Created demo database '{}'", DEMO_DATABASE_NAME);
        }
    }

    private void ensureSchemaAndDataSeeded(String demoJdbcUrl) throws SQLException, IOException {
        try (Connection connection = DriverManager.getConnection(demoJdbcUrl, adminUsername, adminPassword)) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 FROM information_schema.tables WHERE table_name = ?")) {
                ps.setString(1, SENTINEL_TABLE);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return;
                    }
                }
            }
            for (String sql : readSeedStatements()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            }
            log.info("Seeded ride-hailing demo schema and data into '{}'", DEMO_DATABASE_NAME);
        }
    }

    private void ensureRegisteredAsMonitoredDatabase(String demoJdbcUrl) {
        boolean alreadyRegistered = listUseCase.listAll().stream()
                .anyMatch(db -> db.getName().equals(MONITORED_DATABASE_NAME));
        if (alreadyRegistered) {
            return;
        }
        Optional<AdminUser> founder = adminUserRepository.findEarliestByRole(AdminRole.DB_ADMIN);
        if (founder.isEmpty()) {
            log.warn("No DB_ADMIN exists yet — skipping demo database registration (DevDataSeeder should have run"
                    + " first)");
            return;
        }
        registerUseCase.register(new RegisterMonitoredDatabaseUseCase.RegisterDatabaseCommand(
                MONITORED_DATABASE_NAME, DatabaseEngine.POSTGRESQL, demoJdbcUrl, adminUsername, adminPassword,
                founder.get().getId().value()));
        log.info("Registered '{}' as a monitored database owned by {}", MONITORED_DATABASE_NAME,
                founder.get().getEmail());
    }

    /** Splits on ';' — safe here because the seed script has no semicolons inside string literals,
     * comments, or expressions (checked at authoring time; not a general-purpose SQL splitter). */
    private String[] readSeedStatements() throws IOException {
        try (InputStream in = new ClassPathResource(SEED_SCRIPT_PATH).getInputStream()) {
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return Arrays.stream(script.split(";"))
                    .map(String::strip)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
    }

    private String withDatabaseName(String jdbcUrl, String databaseName) {
        int lastSlash = jdbcUrl.lastIndexOf('/');
        int queryStart = jdbcUrl.indexOf('?', lastSlash);
        String suffix = queryStart >= 0 ? jdbcUrl.substring(queryStart) : "";
        return jdbcUrl.substring(0, lastSlash + 1) + databaseName + suffix;
    }
}
