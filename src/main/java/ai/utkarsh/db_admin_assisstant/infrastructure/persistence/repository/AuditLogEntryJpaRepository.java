package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AuditLogEntryEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogEntryJpaRepository extends JpaRepository<AuditLogEntryEntity, UUID> {

    List<AuditLogEntryEntity> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, String entityId,
            Limit limit);

    List<AuditLogEntryEntity> findAllByOrderByOccurredAtDesc(Limit limit);
}
