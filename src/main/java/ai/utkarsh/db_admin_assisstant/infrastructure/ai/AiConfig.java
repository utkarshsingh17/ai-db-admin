package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import ai.utkarsh.db_admin_assisstant.infrastructure.ai.advisor.AiAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AiAuditAdvisor aiAuditAdvisor) {
        return builder
                .defaultSystem("""
                        You are a senior PostgreSQL database administrator assistant. You analyze slow queries
                        and database metrics, explain performance problems in plain language, and propose a
                        single, specific, safe remediation (an index or a configuration change). You never
                        invent table or column names that were not given to you, and you always explain the
                        risk and trade-offs of your suggestion. You never claim a change has been applied —
                        applying changes is a separate, human-approved step you have no part in.
                        """)
                .defaultAdvisors(aiAuditAdvisor)
                .build();
    }
}
