package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;

import java.util.Locale;

/**
 * {@code CREATE INDEX CONCURRENTLY} must run outside a transaction — the executor adapter handles
 * that JDBC detail. This command's own job is narrower: refuse to wrap anything that isn't actually
 * an index-shaped statement.
 */
public final class CreateIndexCommand implements DatabaseChangeCommand {

    private final Sql sql;

    public CreateIndexCommand(Sql sql) {
        if (!sql.statement().toUpperCase(Locale.ROOT).startsWith("CREATE INDEX")) {
            throw new IllegalArgumentException(
                    "CreateIndexCommand requires a CREATE INDEX statement, got: " + sql.statement());
        }
        ExecutableStatementGuard.requireSingleStatement(sql);
        this.sql = sql;
    }

    @Override
    public DatabaseChangeExecutorPort.ExecutionResult execute(MonitoredDatabase target,
            DatabaseChangeExecutorPort executor) {
        return executor.execute(target, sql);
    }
}
