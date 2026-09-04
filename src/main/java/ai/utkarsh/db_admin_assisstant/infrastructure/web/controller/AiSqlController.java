package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.query.model.AiQuerySubmission;
import ai.utkarsh.db_admin_assisstant.domain.query.port.in.AskAiSqlQuestionUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.AiQuerySubmissionResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.AskAiQuestionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/databases")
@RequiredArgsConstructor
public class AiSqlController {

    private final AskAiSqlQuestionUseCase askAiSqlQuestionUseCase;
    private final CurrentAdminResolver currentAdminResolver;

    @PostMapping("/{id}/ai-query")
    public ApiResponse<AiQuerySubmissionResponse> ask(@PathVariable String id,
            @Valid @RequestBody AskAiQuestionRequest request, Authentication authentication) {
        AiQuerySubmission submission = askAiSqlQuestionUseCase.ask(new AskAiSqlQuestionUseCase.AskAiSqlQuestionCommand(
                DatabaseId.of(id), request.question(), currentAdminResolver.resolveId(authentication)));
        return ApiResponse.ok(AiQuerySubmissionResponse.from(submission));
    }
}
