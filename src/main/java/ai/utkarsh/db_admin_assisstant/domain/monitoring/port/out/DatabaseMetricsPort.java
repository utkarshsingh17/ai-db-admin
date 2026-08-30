package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseEngine;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;

/**
 * Strategy port — one implementation per database engine. {@code MetricsCollectionService} picks the
 * implementation whose {@link #supportedEngine()} matches the target {@link MonitoredDatabase}.
 */
public interface DatabaseMetricsPort {

    DatabaseEngine supportedEngine();

    CollectedMetrics collect(MonitoredDatabase database);
}
