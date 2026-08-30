package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import java.util.Objects;

/**
 * Non-blank text — for {@link RecommendationType#INDEX} and {@link RecommendationType#CONFIG_CHANGE}
 * this is the vetted DDL statement; for {@link RecommendationType#QUERY_REWRITE} it's advisory-only
 * text (a suggested rewritten query, which may legitimately contain a trailing {@code ;} or other
 * punctuation) that is never executed.
 *
 * <p>This type only guards against blank text. The actual executability gates — the allow-listed
 * {@code CREATE INDEX CONCURRENTLY} / {@code ALTER SYSTEM SET} prefixes, and the single-statement
 * (no {@code ;}) requirement — live in {@code CreateIndexCommand} and
 * {@code UpdateConfigParameterCommand}, which independently validate before anything reaches the
 * JDBC executor. Enforcing either rule here too would make it impossible to construct a
 * {@code QUERY_REWRITE} recommendation, which is free-form advisory text by design.
 */
public record Sql(String statement) {

    public Sql {
        Objects.requireNonNull(statement, "statement must not be null");
        String trimmed = statement.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Sql statement must not be blank");
        }
        statement = trimmed;
    }
}
