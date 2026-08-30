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

/** Dev-only seed data — never via Flyway (see the flyway-migrations skill's own guidance). */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final AdminUserJpaRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() == 0) {
            AdminUserEntity admin = new AdminUserEntity();
            admin.setId(UUID.randomUUID());
            admin.setEmail("admin@dev.local");
            admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
            admin.setRole("DB_ADMIN");
            admin.setEnabled(true);
            admin.setCreatedAt(Instant.now());
            admin.setUpdatedAt(Instant.now());
            adminUserRepository.save(admin);
        }
    }
}
