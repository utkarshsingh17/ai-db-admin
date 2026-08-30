package ai.utkarsh.db_admin_assisstant.infrastructure.monitoring.postgres;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseEngine;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.CollectedMetrics;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.DatabaseMetricsPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy implementation for PostgreSQL — opens its own short-lived JDBC connection to the target
 * (never the app's own DataSource, since the target is an arbitrary registered database). Instance
 * metrics come from {@code pg_stat_activity}/{@code pg_settings}/{@code pg_stat_database}/
 * {@code pg_locks}; slow queries come from {@code pg_stat_statements}, which degrades gracefully
 * (empty list, not a failure) if that extension isn't installed on the target.
 */
@Component
@Slf4j
public class PostgresMetricsCollector implements DatabaseMetricsPort {

    private final double slowQueryThresholdMs;

    public PostgresMetricsCollector(@Value("${app.monitoring.slow-query-threshold-ms}") double slowQueryThresholdMs) {
        this.slowQueryThresholdMs = slowQueryThresholdMs;
    }

    @Override
    public DatabaseEngine supportedEngine() {
        return DatabaseEngine.POSTGRESQL;
    }

    @Override
    public CollectedMetrics collect(MonitoredDatabase database) {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUsername(),
                database.getPassword())) {
            Integer activeConnections = queryInt(connection,
                    "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'");
            Integer maxConnections = queryInt(connection, "SHOW max_connections");
            Double cacheHitRatio = queryDouble(connection, """
                    SELECT CASE WHEN sum(blks_hit) + sum(blks_read) = 0 THEN NULL
                           ELSE sum(blks_hit)::float8 / (sum(blks_hit) + sum(blks_read)) END
                    FROM pg_stat_database
                    """);
            Integer lockWaitCount = queryInt(connection, "SELECT count(*) FROM pg_locks WHERE NOT granted");
            List<CollectedMetrics.RawSlowQuery> slowQueries = collectSlowQueries(connection);

            return new CollectedMetrics(activeConnections, maxConnections, cacheHitRatio, lockWaitCount,
                    Instant.now(), slowQueries);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to collect metrics for database " + database.getName(), e);
        }
    }

    private List<CollectedMetrics.RawSlowQuery> collectSlowQueries(Connection connection) {
        String sql = """
                SELECT queryid, query, calls, mean_exec_time, total_exec_time, rows
                FROM pg_stat_statements
                WHERE mean_exec_time >= ?
                ORDER BY mean_exec_time DESC
                LIMIT 20
                """;
        List<CollectedMetrics.RawSlowQuery> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, slowQueryThresholdMs);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new CollectedMetrics.RawSlowQuery(String.valueOf(rs.getLong("queryid")),
                            rs.getString("query"), rs.getLong("calls"), rs.getDouble("mean_exec_time"),
                            rs.getDouble("total_exec_time"), rs.getLong("rows")));
                }
            }
        } catch (SQLException e) {
            log.warn("pg_stat_statements unavailable on target database — slow query capture skipped: {}",
                    e.getMessage());
        }
        return results;
    }

    private Integer queryInt(Connection connection, String sql) throws SQLException {
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }

    private Double queryDouble(Connection connection, String sql) throws SQLException {
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (!rs.next()) {
                return null;
            }
            double value = rs.getDouble(1);
            return rs.wasNull() ? null : value;
        }
    }
}
