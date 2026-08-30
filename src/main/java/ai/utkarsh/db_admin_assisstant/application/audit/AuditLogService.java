package ai.utkarsh.db_admin_assisstant.application.audit;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditLogEntry;
import ai.utkarsh.db_admin_assisstant.domain.audit.port.out.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes append-only audit entries. Always {@code REQUIRES_NEW} so an audit record survives even if
 * the caller's own transaction later rolls back — see the transactional-patterns skill's "audit
 * logging that must survive rollback" example.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, AuditAction action, String entityType, String entityId, String payload,
            String correlationId) {
        AuditLogEntry entry = AuditLogEntry.record(actor, action, entityType, entityId, payload, correlationId);
        auditLogRepository.save(entry);
    }
}
