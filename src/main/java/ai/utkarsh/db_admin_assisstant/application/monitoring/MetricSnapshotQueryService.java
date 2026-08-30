package ai.utkarsh.db_admin_assisstant.application.monitoring;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshot;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.ListMetricSnapshotsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MetricSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricSnapshotQueryService implements ListMetricSnapshotsUseCase {

    private final MetricSnapshotRepository metricSnapshotRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MetricSnapshot> listRecent(DatabaseId databaseId, int limit) {
        return metricSnapshotRepository.findRecentByDatabase(databaseId, limit);
    }
}
