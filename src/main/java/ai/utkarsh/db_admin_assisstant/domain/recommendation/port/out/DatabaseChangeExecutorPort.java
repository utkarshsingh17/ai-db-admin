package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;

/** Executes an already-approved, already-validated {@link Sql} statement against a target database. */
public interface DatabaseChangeExecutorPort {

    ExecutionResult execute(MonitoredDatabase target, Sql sql);

    record ExecutionResult(boolean success, String message) {

        public static ExecutionResult ok() {
            return new ExecutionResult(true, null);
        }

        public static ExecutionResult failure(String message) {
            return new ExecutionResult(false, message);
        }
    }
}
