package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper;

import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditLogEntry;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AuditLogEntryEntity;

public final class AuditLogEntryMapper {

    private AuditLogEntryMapper() {
    }

    public static AuditLogEntry toDomain(AuditLogEntryEntity entity) {
        return AuditLogEntry.reconstitute(entity.getId(), entity.getActor(), entity.getAction(),
                entity.getEntityType(), entity.getEntityId(), entity.getPayload(), entity.getCorrelationId(),
                entity.getOccurredAt());
    }

    public static AuditLogEntryEntity toEntity(AuditLogEntry domain) {
        AuditLogEntryEntity entity = new AuditLogEntryEntity();
        entity.setId(domain.getId());
        entity.setActor(domain.getActor());
        entity.setAction(domain.getAction());
        entity.setEntityType(domain.getEntityType());
        entity.setEntityId(domain.getEntityId());
        entity.setPayload(domain.getPayload());
        entity.setCorrelationId(domain.getCorrelationId());
        entity.setOccurredAt(domain.getOccurredAt());
        return entity;
    }
}
