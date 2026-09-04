package ai.utkarsh.db_admin_assisstant.domain.query.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.query.model.AiQuerySubmission;

import java.util.UUID;

public interface AskAiSqlQuestionUseCase {

    AiQuerySubmission ask(AskAiSqlQuestionCommand command);

    record AskAiSqlQuestionCommand(DatabaseId databaseId, String question, UUID actingAdminId) {
    }
}
