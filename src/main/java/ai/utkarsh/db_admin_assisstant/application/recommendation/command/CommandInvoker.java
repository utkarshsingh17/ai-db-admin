package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;
import org.springframework.stereotype.Component;

/** Invoker in the Command pattern — the only place a {@link DatabaseChangeCommand} is actually run. */
@Component
public class CommandInvoker {

    private final DatabaseChangeExecutorPort executor;

    public CommandInvoker(DatabaseChangeExecutorPort executor) {
        this.executor = executor;
    }

    public DatabaseChangeExecutorPort.ExecutionResult invoke(DatabaseChangeCommand command,
            MonitoredDatabase target) {
        return command.execute(target, executor);
    }
}
