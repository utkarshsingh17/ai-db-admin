package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import ai.utkarsh.db_admin_assisstant.domain.query.port.out.GeneratedSql;
import ai.utkarsh.db_admin_assisstant.domain.query.port.out.NaturalLanguageToSqlPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Adapter for the domain's {@link NaturalLanguageToSqlPort}, mirroring
 * {@link SpringAiRecommendationAdapter}'s shape. Overrides the {@code ChatClient} bean's default
 * system prompt (which is about drafting performance recommendations) with one specific to safe,
 * read-only SQL generation — the code-level enforcement is
 * {@code SqlStatementClassifier#requireSelectOnly}, applied by the caller; this system prompt is
 * defense in depth, not the enforcement itself.
 */
@Component
public class SpringAiNlToSqlAdapter implements NaturalLanguageToSqlPort {

    private static final String SYSTEM_PROMPT = """
            You are a PostgreSQL read-only query generator. You only ever produce a single SELECT
            statement that reads data. You never produce INSERT, UPDATE, DELETE, DROP, ALTER,
            TRUNCATE, GRANT, REVOKE, or any statement that modifies data, schema, or configuration —
            even if the user explicitly asks you to. You only reference tables and columns you were
            given; you never invent one.
            """;

    private final ChatClient chatClient;
    private final Resource promptTemplate;

    public SpringAiNlToSqlAdapter(ChatClient chatClient,
            @Value("classpath:prompts/nl-to-sql.st") Resource promptTemplate) {
        this.chatClient = chatClient;
        this.promptTemplate = promptTemplate;
    }

    @Retryable(includes = TransientAiException.class, maxRetries = 2, delay = 500, multiplier = 2.0, jitter = 100)
    @Override
    public GeneratedSql translate(String databaseName, String schemaSummary, String question) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(u -> u.text(promptTemplate)
                        .param("databaseName", databaseName)
                        .param("schemaSummary", schemaSummary)
                        .param("question", question))
                .call()
                .entity(GeneratedSql.class);
    }
}
