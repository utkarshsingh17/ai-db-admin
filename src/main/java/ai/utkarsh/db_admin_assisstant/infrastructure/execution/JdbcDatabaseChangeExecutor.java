package ai.utkarsh.db_admin_assisstant.infrastructure.execution;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The only place in the system that actually runs a change against a monitored database — and only
 * ever with a {@link Sql} value object, which already passed the allow-list validation in its
 * constructor. Runs with {@code autoCommit=true} because {@code CREATE INDEX CONCURRENTLY} must
 * execute outside a transaction block in PostgreSQL.
 */
@Component
@Slf4j
public class JdbcDatabaseChangeExecutor implements DatabaseChangeExecutorPort {

    @Override
    public ExecutionResult execute(MonitoredDatabase target, Sql sql) {
        try (Connection connection = DriverManager.getConnection(target.getJdbcUrl(), target.getUsername(),
                target.getPassword())) {
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql.statement());
            }
            log.info("Applied database change on {}: {}", target.getName(), sql.statement());
            return ExecutionResult.ok();
        } catch (SQLException e) {
            log.error("Failed to apply database change on {}: {}", target.getName(), sql.statement(), e);
            return ExecutionResult.failure(e.getMessage());
        }
    }
}
