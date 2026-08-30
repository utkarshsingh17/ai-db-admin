package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.adapter;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshot;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MetricSnapshotRepository;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper.MetricSnapshotMapper;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.MetricSnapshotJpaRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Repository
public class JpaMetricSnapshotRepository implements MetricSnapshotRepository {

    private final MetricSnapshotJpaRepository springDataRepository;

    public JpaMetricSnapshotRepository(MetricSnapshotJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public MetricSnapshot save(MetricSnapshot snapshot) {
        return MetricSnapshotMapper.toDomain(springDataRepository.save(MetricSnapshotMapper.toEntity(snapshot)));
    }

    @Override
    public List<MetricSnapshot> findRecentByDatabase(DatabaseId databaseId, int limit) {
        return springDataRepository.findByDatabaseIdOrderByCapturedAtDesc(databaseId.value(), Limit.of(limit))
                .stream().map(MetricSnapshotMapper::toDomain).toList();
    }

    @Override
    public int deleteOlderThanDays(int days) {
        return springDataRepository.deleteByCapturedAtBefore(Instant.now().minus(days, ChronoUnit.DAYS));
    }
}
