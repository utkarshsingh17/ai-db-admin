package ai.utkarsh.db_admin_assisstant.infrastructure.execution;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import ai.utkarsh.db_admin_assisstant.domain.query.port.out.ReadOnlyQueryExecutorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs an already-classified read-only statement against a monitored database. Same short-lived
 * {@code DriverManager} connection pattern as {@code JdbcDatabaseChangeExecutor}, plus the first
 * row-cap/timeout safety mechanisms in the codebase, since this is the first place arbitrary
 * admin-authored SQL is run against a live result set rather than a fixed, developer-written query.
 */
@Component
@Slf4j
public class JdbcReadOnlyQueryExecutor implements ReadOnlyQueryExecutorPort {

    private final int maxRows;
    private final int timeoutSeconds;

    public JdbcReadOnlyQueryExecutor(@Value("${app.query-editor.max-rows}") int maxRows,
            @Value("${app.query-editor.timeout-seconds}") int timeoutSeconds) {
        this.maxRows = maxRows;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public QueryResult execute(MonitoredDatabase target, String sql) {
        long start = System.currentTimeMillis();
        try (Connection connection = DriverManager.getConnection(target.getJdbcUrl(), target.getUsername(),
                target.getPassword())) {
            try (Statement statement = connection.createStatement()) {
                statement.setMaxRows(maxRows);
                statement.setQueryTimeout(timeoutSeconds);
                try (ResultSet rs = statement.executeQuery(sql)) {
                    return toQueryResult(rs, System.currentTimeMillis() - start);
                }
            }
        } catch (SQLException e) {
            log.warn("Query editor execution failed on {}: {}", target.getName(), e.getMessage());
            throw new IllegalArgumentException(
                    "Query failed: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    private QueryResult toQueryResult(ResultSet rs, long executionTimeMs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<String> columns = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        List<List<String>> rows = new ArrayList<>();
        int count = 0;
        while (rs.next() && count < maxRows) {
            List<String> row = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                row.add(value == null ? null : String.valueOf(value));
            }
            rows.add(row);
            count++;
        }
        // Statement.setMaxRows already caps the driver's result set at maxRows, so there is no
        // further row to peek at here — treat hitting the cap exactly as a (best-effort) truncation
        // signal for the UI.
        boolean truncated = count >= maxRows;
        return new QueryResult(columns, rows, count, truncated, executionTimeMs);
    }
}
