package ai.utkarsh.db_admin_assisstant.application.query;

import ai.utkarsh.db_admin_assisstant.application.audit.AuditLogService;
import ai.utkarsh.db_admin_assisstant.application.audit.JsonPayload;
import ai.utkarsh.db_admin_assisstant.application.shared.SqlStatementClassifier;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabaseNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import ai.utkarsh.db_admin_assisstant.domain.query.model.AiQuerySubmission;
import ai.utkarsh.db_admin_assisstant.domain.query.port.in.AskAiSqlQuestionUseCase;
import ai.utkarsh.db_admin_assisstant.domain.query.port.out.GeneratedSql;
import ai.utkarsh.db_admin_assisstant.domain.query.port.out.NaturalLanguageToSqlPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationType;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RiskLevel;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.Sql;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.PerformanceRecommendationRepository;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.DatabaseSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.TableSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.port.out.SchemaIntrospectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Drafts a SQL query from a natural-language question and submits it for admin approval — it never
 * executes here. See {@code RecommendationOrchestrationService#applyAiQuery} for the approval →
 * execution step, which reuses the exact {@code PerformanceRecommendation} approve/apply lifecycle
 * already built for AI-drafted performance recommendations and manually-submitted SQL.
 */
@Service
@RequiredArgsConstructor
public class AiSqlAssistantService implements AskAiSqlQuestionUseCase {

    private static final Pattern CODE_FENCE = Pattern.compile("```(?:sql)?\\s*|```", Pattern.CASE_INSENSITIVE);

    private final MonitoredDatabaseRepository monitoredDatabaseRepository;
    private final SchemaIntrospectionPort schemaIntrospectionPort;
    private final NaturalLanguageToSqlPort naturalLanguageToSqlPort;
    private final PerformanceRecommendationRepository recommendationRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AiQuerySubmission ask(AskAiSqlQuestionCommand command) {
        MonitoredDatabase database = monitoredDatabaseRepository.findById(command.databaseId())
                .orElseThrow(() -> new MonitoredDatabaseNotFoundException(command.databaseId()));

        DatabaseSchema schema = schemaIntrospectionPort.introspect(database);
        String schemaSummary = summarize(schema);

        GeneratedSql generated = naturalLanguageToSqlPort.translate(database.getName(), schemaSummary,
                command.question());
        String sql = generated.sql() == null ? "" : CODE_FENCE.matcher(generated.sql()).replaceAll("").strip();

        AiQuerySubmission submission;
        if (sql.isBlank()) {
            submission = new AiQuerySubmission(null, generated.explanation(), null);
        } else {
            SqlStatementClassifier.requireSelectOnly(sql);
            SqlStatementClassifier.requireSingleStatement(sql);

            PerformanceRecommendation recommendation = PerformanceRecommendation.draft(command.databaseId(), null,
                    RecommendationType.AI_QUERY, RiskLevel.LOW, command.question(), generated.explanation(),
                    new Sql(sql), null);
            recommendation.submitForApproval();
            recommendation.pullDomainEvents();
            PerformanceRecommendation saved = recommendationRepository.save(recommendation);

            submission = new AiQuerySubmission(sql, generated.explanation(), saved.getId());
        }

        auditLogService.record(command.actingAdminId().toString(), AuditAction.AI_SQL_QUERY_ASKED,
                "MonitoredDatabase", command.databaseId().value().toString(),
                JsonPayload.of().put("question", command.question()).put("sql", submission.sql()).build(), null);
        return submission;
    }

    private String summarize(DatabaseSchema schema) {
        StringBuilder sb = new StringBuilder();
        for (TableSchema table : schema.tables()) {
            sb.append(table.name()).append('(');
            sb.append(String.join(", ", table.columns().stream()
                    .map(c -> c.name() + " " + c.dataType() + (c.primaryKey() ? " PK" : ""))
                    .toList()));
            sb.append(")\n");
        }
        return sb.toString();
    }
}
