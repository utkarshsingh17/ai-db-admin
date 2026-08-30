package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateConfigParameterCommandTest {

    @Test
    void acceptsAlterSystemSetStatement() {
        Sql sql = new Sql("ALTER SYSTEM SET work_mem = '64MB'");

        assertThatCode(() -> new UpdateConfigParameterCommand(sql)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNonConfigStatement() {
        Sql sql = new Sql("CREATE INDEX CONCURRENTLY idx_orders_customer_id ON orders(customer_id)");

        assertThatThrownBy(() -> new UpdateConfigParameterCommand(sql)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMultipleStatements() {
        Sql sql = new Sql("ALTER SYSTEM SET work_mem = '64MB'; DROP TABLE orders");

        assertThatThrownBy(() -> new UpdateConfigParameterCommand(sql)).isInstanceOf(IllegalArgumentException.class);
    }
}
