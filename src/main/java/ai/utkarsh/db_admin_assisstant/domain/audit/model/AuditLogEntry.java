package ai.utkarsh.db_admin_assisstant.domain.audit.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable, append-only audit record — the compliance trail for every recommendation and every
 * action taken on it. Deliberately has no setters and {@link ai.utkarsh.db_admin_assisstant.domain.audit.port.out.AuditLogRepository}
 * exposes no update/delete method.
 */
public final class AuditLogEntry {

    private final UUID id;
    private final String actor; // "SYSTEM" for AI-originated actions, admin user id otherwise
    private final AuditAction action;
    private final String entityType;
    private final String entityId;
    private final String payload; // JSON snapshot of the relevant state/diff
    private final String correlationId;
    private final Instant occurredAt;

    private AuditLogEntry(UUID id, String actor, AuditAction action, String entityType, String entityId,
            String payload, String correlationId, Instant occurredAt) {
        this.id = id;
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.payload = payload;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
    }

    public static AuditLogEntry record(String actor, AuditAction action, String entityType, String entityId,
            String payload, String correlationId) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(entityType, "entityType must not be null");
        Objects.requireNonNull(entityId, "entityId must not be null");
        return new AuditLogEntry(UUID.randomUUID(), actor, action, entityType, entityId, payload, correlationId,
                Instant.now());
    }

    public static AuditLogEntry reconstitute(UUID id, String actor, AuditAction action, String entityType,
            String entityId, String payload, String correlationId, Instant occurredAt) {
        return new AuditLogEntry(id, actor, action, entityType, entityId, payload, correlationId, occurredAt);
    }

    public UUID getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getPayload() {
        return payload;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
