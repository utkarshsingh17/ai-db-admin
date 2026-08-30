package ai.utkarsh.db_admin_assisstant.application.audit;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditLogEntry;
import ai.utkarsh.db_admin_assisstant.domain.audit.port.in.ListAuditLogUseCase;
import ai.utkarsh.db_admin_assisstant.domain.audit.port.out.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogQueryService implements ListAuditLogUseCase {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogEntry> listByEntity(String entityType, String entityId, int limit) {
        return auditLogRepository.findByEntity(entityType, entityId, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogEntry> listRecent(int limit) {
        return auditLogRepository.findRecent(limit);
    }
}
