package ai.utkarsh.db_admin_assisstant.config;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.service.SlowQueryClassifier;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.service.RecommendationFactory;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.service.RecommendationRiskAssessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the stateless, framework-free domain services as beans so application services can receive them via DI. */
@Configuration
public class DomainServiceConfig {

    @Bean
    public SlowQueryClassifier slowQueryClassifier(@Value("${app.monitoring.slow-query-threshold-ms}") double thresholdMs) {
        return new SlowQueryClassifier(thresholdMs);
    }

    @Bean
    public RecommendationRiskAssessor recommendationRiskAssessor() {
        return new RecommendationRiskAssessor();
    }

    @Bean
    public RecommendationFactory recommendationFactory(RecommendationRiskAssessor riskAssessor) {
        return new RecommendationFactory(riskAssessor);
    }
}
