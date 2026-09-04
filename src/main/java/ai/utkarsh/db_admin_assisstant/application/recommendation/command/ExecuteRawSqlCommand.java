package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;

/**
 * Runs an admin-authored SQL statement submitted through the SQL editor's write path, once
 * approved. Unlike {@link CreateIndexCommand}/{@link UpdateConfigParameterCommand} this has no
 * prefix allow-list — it's arbitrary DDL/DML a human admin wrote and another admin approved — but
 * it still enforces the single-statement rule before anything reaches the JDBC executor.
 */
public final class ExecuteRawSqlCommand implements DatabaseChangeCommand {

    private final Sql sql;

    public ExecuteRawSqlCommand(Sql sql) {
        ExecutableStatementGuard.requireSingleStatement(sql);
        this.sql = sql;
    }

    @Override
    public DatabaseChangeExecutorPort.ExecutionResult execute(MonitoredDatabase target,
            DatabaseChangeExecutorPort executor) {
        return executor.execute(target, sql);
    }
}
