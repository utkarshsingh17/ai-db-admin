package ai.utkarsh.db_admin_assisstant.domain.recommendation.service;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationType;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RiskLevel;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationRiskAssessorTest {

    private final RecommendationRiskAssessor assessor = new RecommendationRiskAssessor();

    @Test
    void configChange_isAlwaysHighRisk() {
        Sql sql = new Sql("ALTER SYSTEM SET work_mem = '64MB'");

        RiskLevel risk = assessor.assess(RecommendationType.CONFIG_CHANGE, sql);

        assertThat(risk).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void index_isMediumRisk() {
        Sql sql = new Sql("CREATE INDEX CONCURRENTLY idx_orders_customer_id ON orders(customer_id)");

        RiskLevel risk = assessor.assess(RecommendationType.INDEX, sql);

        assertThat(risk).isEqualTo(RiskLevel.MEDIUM);
    }
}
