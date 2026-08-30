package ai.utkarsh.db_admin_assisstant.domain.audit.port.out;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditLogEntry;

import java.util.List;

/** Append-only — deliberately exposes no update or delete method. */
public interface AuditLogRepository {

    AuditLogEntry save(AuditLogEntry entry);

    List<AuditLogEntry> findByEntity(String entityType, String entityId, int limit);

    List<AuditLogEntry> findRecent(int limit);
}
