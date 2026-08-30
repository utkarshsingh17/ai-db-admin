package ai.utkarsh.db_admin_assisstant.application.monitoring;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshot;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.QueryFingerprint;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.CollectedMetrics;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MetricSnapshotRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.SlowQueryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists one poll cycle's results. Kept as its own bean (rather than a private method on
 * {@code MetricsCollectionService}) so {@code @Transactional} actually applies — calling a
 * {@code @Transactional} method on {@code this} bypasses the Spring proxy.
 */
@Service
@RequiredArgsConstructor
public class MetricsPersistenceService {

    private final MetricSnapshotRepository metricSnapshotRepository;
    private final SlowQueryEventRepository slowQueryEventRepository;

    @Transactional
    public List<SlowQueryEvent> persist(MonitoredDatabase database, CollectedMetrics collected) {
        MetricSnapshot snapshot = MetricSnapshot.capture(database.getId(), collected.activeConnections(),
                collected.maxConnections(), collected.cacheHitRatio(), collected.lockWaitCount(),
                collected.capturedAt());
        metricSnapshotRepository.save(snapshot);

        List<SlowQueryEvent> saved = new ArrayList<>();
        for (CollectedMetrics.RawSlowQuery raw : collected.slowQueries()) {
            SlowQueryEvent event = SlowQueryEvent.capture(database.getId(),
                    new QueryFingerprint(raw.queryFingerprint()), raw.normalizedQuery(), raw.calls(),
                    raw.meanExecTimeMs(), raw.totalExecTimeMs(), raw.rowsReturned(), collected.capturedAt());
            saved.add(slowQueryEventRepository.save(event));
        }
        return saved;
    }
}
