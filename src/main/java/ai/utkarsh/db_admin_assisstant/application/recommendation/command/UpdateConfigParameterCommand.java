package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;

import java.util.Locale;

/** {@code ALTER SYSTEM SET} — always HIGH risk (see RecommendationRiskAssessor); may require a reload. */
public final class UpdateConfigParameterCommand implements DatabaseChangeCommand {

    private final Sql sql;

    public UpdateConfigParameterCommand(Sql sql) {
        if (!sql.statement().toUpperCase(Locale.ROOT).startsWith("ALTER SYSTEM SET")) {
            throw new IllegalArgumentException(
                    "UpdateConfigParameterCommand requires an ALTER SYSTEM SET statement, got: " + sql.statement());
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
