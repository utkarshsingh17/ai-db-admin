package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AdminUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminUserJpaRepository extends JpaRepository<AdminUserEntity, UUID> {

    Optional<AdminUserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoleAndEnabledTrue(String role);
}
