package ai.utkarsh.db_admin_assisstant.infrastructure.persistence;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AdminUserEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.AdminUserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Dev-only seed data — never via Flyway (see the flyway-migrations skill's own guidance). Ensures
 * each account exists by email rather than gating on "table is empty", so it stays correct across
 * restarts even after other admin users have been created through the app.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "root123";

    private final AdminUserJpaRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        ensureUser("admin@dev.com", "DB_ADMIN");
        ensureUser("viewer@dev.com", "DB_VIEWER");
    }

    private void ensureUser(String email, String role) {
        if (adminUserRepository.existsByEmail(email)) {
            return;
        }
        AdminUserEntity user = new AdminUserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        adminUserRepository.save(user);
    }
}
