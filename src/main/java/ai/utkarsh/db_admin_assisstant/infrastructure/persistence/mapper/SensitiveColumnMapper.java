package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumnId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.SensitiveColumnEntity;

public final class SensitiveColumnMapper {

    private SensitiveColumnMapper() {
    }

    public static SensitiveColumn toDomain(SensitiveColumnEntity entity) {
        return SensitiveColumn.reconstitute(new SensitiveColumnId(entity.getId()),
                new DatabaseId(entity.getDatabaseId()), entity.getTableName(), entity.getColumnName(),
                entity.getCreatedAt());
    }

    public static void updateEntity(SensitiveColumnEntity entity, SensitiveColumn domain) {
        entity.setId(domain.getId().value());
        entity.setDatabaseId(domain.getDatabaseId().value());
        entity.setTableName(domain.getTableName());
        entity.setColumnName(domain.getColumnName());
        entity.setCreatedAt(domain.getCreatedAt());
    }
}
