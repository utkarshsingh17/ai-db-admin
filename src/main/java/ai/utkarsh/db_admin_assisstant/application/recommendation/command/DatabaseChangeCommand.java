package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.DatabaseChangeExecutorPort;

/**
 * Command pattern: built once (at recommendation-draft time, from the AI's vetted SQL) and only
 * invoked later — after an administrator approves it — by {@link CommandInvoker}. Each concrete
 * command also guards that the SQL shape actually matches what that command type expects.
 */
public interface DatabaseChangeCommand {

    DatabaseChangeExecutorPort.ExecutionResult execute(MonitoredDatabase target, DatabaseChangeExecutorPort executor);
}
