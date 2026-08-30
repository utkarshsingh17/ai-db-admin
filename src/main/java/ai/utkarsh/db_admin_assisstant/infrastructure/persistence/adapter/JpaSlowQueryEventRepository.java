package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.adapter;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.SlowQueryEventRepository;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper.SlowQueryEventMapper;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.SlowQueryEventJpaRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaSlowQueryEventRepository implements SlowQueryEventRepository {

    private final SlowQueryEventJpaRepository springDataRepository;

    public JpaSlowQueryEventRepository(SlowQueryEventJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public SlowQueryEvent save(SlowQueryEvent event) {
        return SlowQueryEventMapper.toDomain(springDataRepository.save(SlowQueryEventMapper.toEntity(event)));
    }

    @Override
    public Optional<SlowQueryEvent> findById(SlowQueryEventId id) {
        return springDataRepository.findById(id.value()).map(SlowQueryEventMapper::toDomain);
    }

    @Override
    public List<SlowQueryEvent> findRecentByDatabase(DatabaseId databaseId, int limit) {
        return springDataRepository.findByDatabaseIdOrderByCapturedAtDesc(databaseId.value(), Limit.of(limit))
                .stream().map(SlowQueryEventMapper::toDomain).toList();
    }

    @Override
    public int deleteOlderThanDays(int days) {
        return springDataRepository.deleteByCapturedAtBefore(Instant.now().minus(days, ChronoUnit.DAYS));
    }
}
