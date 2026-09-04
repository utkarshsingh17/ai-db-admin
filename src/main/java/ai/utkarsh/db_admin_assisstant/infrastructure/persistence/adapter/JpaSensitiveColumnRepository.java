package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.adapter;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumnId;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.out.SensitiveColumnRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.SensitiveColumnEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper.SensitiveColumnMapper;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.SensitiveColumnJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaSensitiveColumnRepository implements SensitiveColumnRepository {

    private final SensitiveColumnJpaRepository springDataRepository;

    public JpaSensitiveColumnRepository(SensitiveColumnJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public SensitiveColumn save(SensitiveColumn column) {
        SensitiveColumnEntity entity = springDataRepository.findById(column.getId().value())
                .orElseGet(SensitiveColumnEntity::new);
        SensitiveColumnMapper.updateEntity(entity, column);
        springDataRepository.save(entity);
        return column;
    }

    @Override
    public Optional<SensitiveColumn> findById(SensitiveColumnId id) {
        return springDataRepository.findById(id.value()).map(SensitiveColumnMapper::toDomain);
    }

    @Override
    public List<SensitiveColumn> findByDatabaseId(DatabaseId databaseId) {
        return springDataRepository.findByDatabaseId(databaseId.value()).stream().map(SensitiveColumnMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByDatabaseIdAndTableNameAndColumnName(DatabaseId databaseId, String tableName,
            String columnName) {
        return springDataRepository.existsByDatabaseIdAndTableNameAndColumnName(databaseId.value(), tableName,
                columnName);
    }

    @Override
    public void deleteById(SensitiveColumnId id) {
        springDataRepository.deleteById(id.value());
    }
}
