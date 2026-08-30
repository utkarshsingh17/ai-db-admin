package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditLogEntry;

import java.time.Instant;
import java.util.UUID;

public record AuditLogEntryResponse(UUID id, String actor, String action, String entityType, String entityId,
        String payload, Instant occurredAt) {

    public static AuditLogEntryResponse from(AuditLogEntry entry) {
        return new AuditLogEntryResponse(entry.getId(), entry.getActor(), entry.getAction().name(),
                entry.getEntityType(), entry.getEntityId(), entry.getPayload(), entry.getOccurredAt());
    }
}
