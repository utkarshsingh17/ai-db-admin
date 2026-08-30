package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiAuditLogJpaRepository extends JpaRepository<AiAuditLogEntity, UUID> {
}
