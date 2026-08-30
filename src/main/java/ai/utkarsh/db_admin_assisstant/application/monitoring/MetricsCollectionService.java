package ai.utkarsh.db_admin_assisstant.application.monitoring;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseEngine;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.CollectMetricsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.CollectedMetrics;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.DatabaseMetricsPort;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates one poll cycle: pick the {@link DatabaseMetricsPort} strategy for each database's
 * engine, collect, persist, then hand new slow queries to {@link SlowQueryAnalysisService}. One
 * database's failure never aborts the others.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsCollectionService implements CollectMetricsUseCase {

    private final MonitoredDatabaseRepository monitoredDatabaseRepository;
    private final List<DatabaseMetricsPort> metricsCollectors;
    private final MetricsPersistenceService persistenceService;
    private final SlowQueryAnalysisService slowQueryAnalysisService;

    @Override
    public void collectAll() {
        for (MonitoredDatabase database : monitoredDatabaseRepository.findAllEnabled()) {
            try {
                collectOne(database);
            } catch (Exception e) {
                log.warn("Metrics collection failed for database {}", database.getName(), e);
            }
        }
    }

    private void collectOne(MonitoredDatabase database) {
        DatabaseMetricsPort collector = resolveCollector(database.getEngine());
        CollectedMetrics collected = collector.collect(database);
        List<SlowQueryEvent> newSlowEvents = persistenceService.persist(database, collected);
        slowQueryAnalysisService.analyze(database.getId(), newSlowEvents);
    }

    private DatabaseMetricsPort resolveCollector(DatabaseEngine engine) {
        return metricsCollectors.stream()
                .filter(c -> c.supportedEngine() == engine)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No metrics collector registered for engine " + engine));
    }
}
