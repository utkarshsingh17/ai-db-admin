package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlTest {

    @Test
    void acceptsCreateIndexConcurrently() {
        Sql sql = new Sql("CREATE INDEX CONCURRENTLY idx_orders_customer_id ON orders(customer_id)");

        assertThat(sql.statement()).startsWith("CREATE INDEX CONCURRENTLY");
    }

    @Test
    void acceptsAlterSystemSet() {
        Sql sql = new Sql("ALTER SYSTEM SET work_mem = '64MB'");

        assertThat(sql.statement()).startsWith("ALTER SYSTEM SET");
    }

    @Test
    void acceptsAdvisoryTextForQueryRewriteRecommendations() {
        // Sql itself only guards blank text — QUERY_REWRITE recommendations are advisory-only and
        // never executed, so arbitrary prose (including semicolons) must be constructible here.
        // The executability gates (prefix + single-statement) live in CreateIndexCommand /
        // UpdateConfigParameterCommand — see SqlTest siblings in the command package.
        Sql sql = new Sql("Rewrite as: SELECT id FROM orders WHERE status = 'PENDING' LIMIT 100;");

        assertThat(sql.statement()).contains("Rewrite as");
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new Sql("   ")).isInstanceOf(IllegalArgumentException.class);
    }
}
