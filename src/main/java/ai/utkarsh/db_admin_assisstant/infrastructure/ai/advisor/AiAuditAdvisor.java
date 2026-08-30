package ai.utkarsh.db_admin_assisstant.infrastructure.ai.advisor;

import ai.utkarsh.db_admin_assisstant.infrastructure.ai.AiAuditLogWriter;
import ai.utkarsh.db_admin_assisstant.infrastructure.ai.AiCostEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Decorator on every {@code ChatClient} call: logs and persists request id, latency, token usage and
 * estimated cost for every LLM invocation — independent of the domain-level {@code AuditLogEntry}
 * trail, which covers recommendation lifecycle actions rather than raw model calls.
 */
@Component
public class AiAuditAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(AiAuditAdvisor.class);

    private final AiCostEstimator costEstimator;
    private final AiAuditLogWriter auditLogWriter;

    public AiAuditAdvisor(AiCostEstimator costEstimator, AiAuditLogWriter auditLogWriter) {
        this.costEstimator = costEstimator;
        this.auditLogWriter = auditLogWriter;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        String model = "unknown";

        try {
            ChatClientResponse response = chain.nextCall(request);
            long latency = System.currentTimeMillis() - start;

            int inputTokens = 0;
            int outputTokens = 0;
            ChatResponse chatResponse = response.chatResponse();
            if (chatResponse != null && chatResponse.getMetadata() != null) {
                Usage usage = chatResponse.getMetadata().getUsage();
                if (usage != null) {
                    inputTokens = usage.getPromptTokens();
                    outputTokens = usage.getCompletionTokens();
                }
                if (chatResponse.getMetadata().getModel() != null) {
                    model = chatResponse.getMetadata().getModel();
                }
            }
            double cost = costEstimator.estimateCost(model, inputTokens, outputTokens);
            auditLogWriter.saveAsync(requestId, "chat", model, inputTokens, outputTokens, cost, latency, true, null);
            log.info("[AI-AUDIT] requestId={} latencyMs={} inputTokens={} outputTokens={}", requestId, latency,
                    inputTokens, outputTokens);
            return response;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            auditLogWriter.saveAsync(requestId, "chat", model, 0, 0, 0.0, latency, false, e.getMessage());
            log.error("[AI-AUDIT] requestId={} FAILED after {}ms", requestId, latency, e);
            throw e;
        }
    }

    @Override
    public String getName() {
        return "AiAuditAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
