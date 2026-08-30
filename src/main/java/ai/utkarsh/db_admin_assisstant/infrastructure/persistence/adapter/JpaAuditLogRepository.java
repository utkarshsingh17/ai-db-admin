package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.adapter;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditLogEntry;
import ai.utkarsh.db_admin_assisstant.domain.audit.port.out.AuditLogRepository;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper.AuditLogEntryMapper;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.AuditLogEntryJpaRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaAuditLogRepository implements AuditLogRepository {

    private final AuditLogEntryJpaRepository springDataRepository;

    public JpaAuditLogRepository(AuditLogEntryJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public AuditLogEntry save(AuditLogEntry entry) {
        return AuditLogEntryMapper.toDomain(springDataRepository.save(AuditLogEntryMapper.toEntity(entry)));
    }

    @Override
    public List<AuditLogEntry> findByEntity(String entityType, String entityId, int limit) {
        return springDataRepository
                .findByEntityTypeAndEntityIdOrderByOccurredAtDesc(entityType, entityId, Limit.of(limit)).stream()
                .map(AuditLogEntryMapper::toDomain).toList();
    }

    @Override
    public List<AuditLogEntry> findRecent(int limit) {
        return springDataRepository.findAllByOrderByOccurredAtDesc(Limit.of(limit)).stream()
                .map(AuditLogEntryMapper::toDomain).toList();
    }
}
