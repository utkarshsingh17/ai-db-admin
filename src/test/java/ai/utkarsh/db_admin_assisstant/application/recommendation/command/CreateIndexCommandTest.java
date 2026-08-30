package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateIndexCommandTest {

    @Test
    void acceptsCreateIndexStatement() {
        Sql sql = new Sql("CREATE INDEX CONCURRENTLY idx_orders_customer_id ON orders(customer_id)");

        assertThatCode(() -> new CreateIndexCommand(sql)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNonIndexStatement() {
        // This is the real safety gate now that Sql itself no longer enforces the prefix — a
        // mismatched type/statement recommendation can be created and reviewed, but can never be
        // turned into a runnable command.
        Sql sql = new Sql("ALTER SYSTEM SET work_mem = '64MB'");

        assertThatThrownBy(() -> new CreateIndexCommand(sql)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMultipleStatements() {
        Sql sql = new Sql("CREATE INDEX CONCURRENTLY idx ON t(c); DROP TABLE t");

        assertThatThrownBy(() -> new CreateIndexCommand(sql)).isInstanceOf(IllegalArgumentException.class);
    }
}
