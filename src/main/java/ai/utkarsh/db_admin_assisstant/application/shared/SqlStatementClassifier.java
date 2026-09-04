package ai.utkarsh.db_admin_assisstant.application.shared;

import java.util.Locale;

/**
 * Classifies ad-hoc, admin-authored SQL as read-only vs write for the SQL editor. Deliberately
 * separate from {@code application.recommendation.command.ExecutableStatementGuard} (which guards
 * AI-drafted DDL) to avoid touching that already-tested code path.
 *
 * <p>Read-only is a narrow allow-list ({@code SELECT}/{@code EXPLAIN} prefixes only) — {@code WITH}
 * is deliberately excluded since a common-table-expression can end in a data-modifying clause.
 */
public final class SqlStatementClassifier {

    private SqlStatementClassifier() {
    }

    public static boolean isReadOnly(String sql) {
        String normalized = sql.strip().toUpperCase(Locale.ROOT);
        return normalized.startsWith("SELECT") || normalized.startsWith("EXPLAIN");
    }

    public static void requireReadOnly(String sql) {
        if (!isReadOnly(sql)) {
            throw new IllegalArgumentException(
                    "Only SELECT/EXPLAIN statements can be run here — submit write statements for approval instead");
        }
    }

    public static void requireWrite(String sql) {
        if (isReadOnly(sql)) {
            throw new IllegalArgumentException(
                    "This looks like a read-only statement — run it directly instead of submitting it for approval");
        }
    }

    public static void requireSingleStatement(String sql) {
        if (sql.strip().replaceAll(";\\s*$", "").contains(";")) {
            throw new IllegalArgumentException("Executable statement must be a single statement (no ';'): " + sql);
        }
    }

    /**
     * Stricter than {@link #requireReadOnly} — used for AI-generated SQL, which is less trusted
     * than admin-typed SQL. Only a bare {@code SELECT} passes: no {@code EXPLAIN} (since
     * {@code EXPLAIN ANALYZE} actually executes its inner statement, so it is not truly read-only)
     * and no {@code WITH} (a CTE can end in a data-modifying clause).
     */
    public static void requireSelectOnly(String sql) {
        String normalized = sql.strip().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("SELECT")) {
            throw new IllegalArgumentException("AI-generated SQL must be a single SELECT statement, got: " + sql);
        }
    }
}
