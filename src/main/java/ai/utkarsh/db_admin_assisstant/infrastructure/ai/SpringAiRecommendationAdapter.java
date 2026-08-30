package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.AiRecommendationDraft;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.AiRecommendationPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.SlowQueryAnalysisInput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Adapter for the domain's {@link AiRecommendationPort}. The AI's response is bound directly onto
 * {@link AiRecommendationDraft} — a plain record with no Spring imports — so the domain stays
 * framework-free even though this adapter uses Spring AI's structured-output support to fill it.
 */
@Component
public class SpringAiRecommendationAdapter implements AiRecommendationPort {

    private final ChatClient chatClient;
    private final Resource promptTemplate;

    public SpringAiRecommendationAdapter(ChatClient chatClient,
            @Value("classpath:prompts/recommend-fix.st") Resource promptTemplate) {
        this.chatClient = chatClient;
        this.promptTemplate = promptTemplate;
    }

    @Retryable(includes = TransientAiException.class, maxRetries = 2, delay = 500, multiplier = 2.0, jitter = 100)
    @Override
    public AiRecommendationDraft draftRecommendation(SlowQueryAnalysisInput input) {
        return chatClient.prompt()
                .user(u -> u.text(promptTemplate)
                        .param("databaseName", input.databaseName())
                        .param("normalizedQuery", input.normalizedQuery())
                        .param("calls", input.calls())
                        .param("meanExecTimeMs", input.meanExecTimeMs())
                        .param("totalExecTimeMs", input.totalExecTimeMs()))
                .call()
                .entity(AiRecommendationDraft.class);
    }
}
