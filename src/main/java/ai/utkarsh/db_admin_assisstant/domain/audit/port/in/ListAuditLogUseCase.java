package ai.utkarsh.db_admin_assisstant.domain.audit.port.in;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditLogEntry;

import java.util.List;

public interface ListAuditLogUseCase {

    List<AuditLogEntry> listByEntity(String entityType, String entityId, int limit);

    List<AuditLogEntry> listRecent(int limit);
}
