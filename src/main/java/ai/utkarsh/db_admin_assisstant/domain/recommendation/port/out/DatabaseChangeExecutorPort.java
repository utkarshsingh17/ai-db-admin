package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;

/** Executes an already-approved, already-validated {@link Sql} statement against a target database. */
public interface DatabaseChangeExecutorPort {

    ExecutionResult execute(MonitoredDatabase target, Sql sql);

    /** {@code ALREADY_EXISTS} covers a DDL target (index, table, etc.) that already existed — the
     * desired end state is already true, so it's a resolved outcome, not a failure. */
    record ExecutionResult(Outcome outcome, String message) {

        public enum Outcome {
            SUCCESS,
            ALREADY_EXISTS,
            FAILURE
        }

        public static ExecutionResult ok() {
            return new ExecutionResult(Outcome.SUCCESS, null);
        }

        public static ExecutionResult alreadyExists(String message) {
            return new ExecutionResult(Outcome.ALREADY_EXISTS, message);
        }

        public static ExecutionResult failure(String message) {
            return new ExecutionResult(Outcome.FAILURE, message);
        }
    }
}
