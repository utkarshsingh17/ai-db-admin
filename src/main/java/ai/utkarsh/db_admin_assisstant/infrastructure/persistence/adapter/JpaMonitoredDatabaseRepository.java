package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.adapter;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.MonitoredDatabaseEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper.MonitoredDatabaseMapper;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.MonitoredDatabaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaMonitoredDatabaseRepository implements MonitoredDatabaseRepository {

    private final MonitoredDatabaseJpaRepository springDataRepository;

    public JpaMonitoredDatabaseRepository(MonitoredDatabaseJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public MonitoredDatabase save(MonitoredDatabase database) {
        MonitoredDatabaseEntity entity = springDataRepository.findById(database.getId().value())
                .orElseGet(MonitoredDatabaseEntity::new);
        MonitoredDatabaseMapper.updateEntity(entity, database);
        springDataRepository.save(entity);
        return database;
    }

    @Override
    public Optional<MonitoredDatabase> findById(DatabaseId id) {
        return springDataRepository.findById(id.value()).map(MonitoredDatabaseMapper::toDomain);
    }

    @Override
    public List<MonitoredDatabase> findAllEnabled() {
        return springDataRepository.findByEnabledTrue().stream().map(MonitoredDatabaseMapper::toDomain).toList();
    }

    @Override
    public List<MonitoredDatabase> findAll() {
        return springDataRepository.findAll().stream().map(MonitoredDatabaseMapper::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return springDataRepository.existsByName(name);
    }

    @Override
    public void deleteById(DatabaseId id) {
        // Flush so a foreign-key violation (existing metrics/recommendations/etc. referencing this
        // database) surfaces synchronously here — JPA otherwise defers the DELETE until commit,
        // which would escape the caller's try/catch and surface as an unhandled 500 instead of the
        // clean, translated error the application service expects.
        springDataRepository.deleteById(id.value());
        springDataRepository.flush();
    }
}
