package ai.utkarsh.db_admin_assisstant.application.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlStatementClassifierTest {

    @Test
    void requireSelectOnlyAcceptsSelect() {
        assertThatCode(() -> SqlStatementClassifier.requireSelectOnly("SELECT * FROM demo_orders"))
                .doesNotThrowAnyException();
    }

    @Test
    void requireSelectOnlyAcceptsLowercaseSelect() {
        assertThatCode(() -> SqlStatementClassifier.requireSelectOnly("select id from demo_orders"))
                .doesNotThrowAnyException();
    }

    @Test
    void requireSelectOnlyRejectsExplainAnalyzeWrappingADelete() {
        // EXPLAIN ANALYZE actually executes its inner statement — not truly read-only, so this
        // must be rejected even though the manual editor's looser requireReadOnly allows EXPLAIN.
        assertThatThrownBy(
                () -> SqlStatementClassifier.requireSelectOnly("EXPLAIN ANALYZE DELETE FROM demo_orders"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireSelectOnlyRejectsCte() {
        assertThatThrownBy(() -> SqlStatementClassifier
                .requireSelectOnly("WITH deleted AS (DELETE FROM demo_orders RETURNING *) SELECT * FROM deleted"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireSelectOnlyRejectsWriteStatements() {
        assertThatThrownBy(() -> SqlStatementClassifier.requireSelectOnly("DELETE FROM demo_orders"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SqlStatementClassifier.requireSelectOnly("DROP TABLE demo_orders"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SqlStatementClassifier.requireSelectOnly("UPDATE demo_orders SET status = 'PAID'"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireSingleStatementRejectsMultipleStatements() {
        assertThatThrownBy(() -> SqlStatementClassifier
                .requireSingleStatement("SELECT 1; DROP TABLE demo_orders"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireSingleStatementAllowsTrailingSemicolon() {
        assertThatCode(() -> SqlStatementClassifier.requireSingleStatement("SELECT * FROM demo_orders;"))
                .doesNotThrowAnyException();
    }
}
