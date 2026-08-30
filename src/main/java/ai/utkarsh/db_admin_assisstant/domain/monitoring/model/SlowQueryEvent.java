package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import java.time.Instant;
import java.util.Objects;

/** Aggregate root — one captured occurrence of a slow query shape. */
public class SlowQueryEvent {

    private final SlowQueryEventId id;
    private final DatabaseId databaseId;
    private final QueryFingerprint fingerprint;
    private final String normalizedQuery;
    private final long calls;
    private final double meanExecTimeMs;
    private final double totalExecTimeMs;
    private final Long rowsReturned;
    private final Instant capturedAt;

    private SlowQueryEvent(SlowQueryEventId id, DatabaseId databaseId, QueryFingerprint fingerprint,
            String normalizedQuery, long calls, double meanExecTimeMs, double totalExecTimeMs, Long rowsReturned,
            Instant capturedAt) {
        this.id = id;
        this.databaseId = databaseId;
        this.fingerprint = fingerprint;
        this.normalizedQuery = normalizedQuery;
        this.calls = calls;
        this.meanExecTimeMs = meanExecTimeMs;
        this.totalExecTimeMs = totalExecTimeMs;
        this.rowsReturned = rowsReturned;
        this.capturedAt = capturedAt;
    }

    public static SlowQueryEvent capture(DatabaseId databaseId, QueryFingerprint fingerprint, String normalizedQuery,
            long calls, double meanExecTimeMs, double totalExecTimeMs, Long rowsReturned, Instant capturedAt) {
        Objects.requireNonNull(databaseId, "databaseId must not be null");
        Objects.requireNonNull(fingerprint, "fingerprint must not be null");
        Objects.requireNonNull(normalizedQuery, "normalizedQuery must not be null");
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        return new SlowQueryEvent(SlowQueryEventId.generate(), databaseId, fingerprint, normalizedQuery, calls,
                meanExecTimeMs, totalExecTimeMs, rowsReturned, capturedAt);
    }

    public static SlowQueryEvent reconstitute(SlowQueryEventId id, DatabaseId databaseId,
            QueryFingerprint fingerprint, String normalizedQuery, long calls, double meanExecTimeMs,
            double totalExecTimeMs, Long rowsReturned, Instant capturedAt) {
        return new SlowQueryEvent(id, databaseId, fingerprint, normalizedQuery, calls, meanExecTimeMs,
                totalExecTimeMs, rowsReturned, capturedAt);
    }

    public boolean exceedsThreshold(double thresholdMs) {
        return meanExecTimeMs >= thresholdMs;
    }

    public SlowQueryEventId getId() {
        return id;
    }

    public DatabaseId getDatabaseId() {
        return databaseId;
    }

    public QueryFingerprint getFingerprint() {
        return fingerprint;
    }

    public String getNormalizedQuery() {
        return normalizedQuery;
    }

    public long getCalls() {
        return calls;
    }

    public double getMeanExecTimeMs() {
        return meanExecTimeMs;
    }

    public double getTotalExecTimeMs() {
        return totalExecTimeMs;
    }

    public Long getRowsReturned() {
        return rowsReturned;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }
}
