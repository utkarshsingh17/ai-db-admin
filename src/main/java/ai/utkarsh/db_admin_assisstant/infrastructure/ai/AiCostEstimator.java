package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Prices per million tokens, [input, output] — externalized to {@code app.pricing.*}, not hardcoded. */
@Component
public class AiCostEstimator {

    private static final List<Double> DEFAULT_PRICE = List.of(5.0, 15.0);

    private final Map<String, List<Double>> pricing;

    public AiCostEstimator(Map<String, List<Double>> aiPricing) {
        this.pricing = aiPricing;
    }

    public double estimateCost(String model, int inputTokens, int outputTokens) {
        List<Double> prices = pricing.getOrDefault(model, DEFAULT_PRICE);
        return (inputTokens * prices.get(0) + outputTokens * prices.get(1)) / 1_000_000;
    }
}
