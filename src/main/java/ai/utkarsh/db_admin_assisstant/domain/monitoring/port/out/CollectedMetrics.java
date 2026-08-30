package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out;

import java.time.Instant;
import java.util.List;

/** Raw data returned by a {@link DatabaseMetricsPort} implementation for one poll cycle. */
public record CollectedMetrics(
        Integer activeConnections,
        Integer maxConnections,
        Double cacheHitRatio,
        Integer lockWaitCount,
        Instant capturedAt,
        List<RawSlowQuery> slowQueries) {

    public record RawSlowQuery(
            String queryFingerprint,
            String normalizedQuery,
            long calls,
            double meanExecTimeMs,
            double totalExecTimeMs,
            Long rowsReturned) {
    }
}
