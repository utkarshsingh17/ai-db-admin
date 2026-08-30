package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;

/**
 * Shared pre-execution checks for the two DDL commands. {@link Sql} itself only guards blank text
 * (see its Javadoc) — the prefix and single-statement rules apply only to statements that are
 * actually about to become executable, not to {@code QUERY_REWRITE} advisory text.
 */
final class ExecutableStatementGuard {

    private ExecutableStatementGuard() {
    }

    static void requireSingleStatement(Sql sql) {
        if (sql.statement().contains(";")) {
            throw new IllegalArgumentException(
                    "Executable statement must be a single statement (no ';'): " + sql.statement());
        }
    }
}
