package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AiPricingConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.pricing")
    public Map<String, List<Double>> aiPricing() {
        return new HashMap<>();
    }
}
