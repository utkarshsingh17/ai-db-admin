package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Kept as its own bean (not a method on {@link AiAuditAdvisor}) so {@code @Async} actually applies —
 * calling an {@code @Async} method on {@code this} bypasses the Spring proxy and runs synchronously.
 */
@Component
@RequiredArgsConstructor
public class AiAuditLogWriter {

    private final AiAuditLogJpaRepository repository;

    @Async
    public void saveAsync(String requestId, String operation, String model, int inputTokens, int outputTokens,
            double estimatedCostUsd, long latencyMs, boolean success, String errorMessage) {
        AiAuditLogEntity entity = new AiAuditLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setRequestId(requestId);
        entity.setOperation(operation);
        entity.setModel(model);
        entity.setInputTokens(inputTokens);
        entity.setOutputTokens(outputTokens);
        entity.setEstimatedCostUsd(estimatedCostUsd);
        entity.setLatencyMs(latencyMs);
        entity.setSuccess(success);
        entity.setErrorMessage(errorMessage);
        entity.setCreatedAt(Instant.now());
        repository.save(entity);
    }
}
