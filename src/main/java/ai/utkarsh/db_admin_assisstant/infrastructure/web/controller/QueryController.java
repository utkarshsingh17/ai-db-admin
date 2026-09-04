package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import ai.utkarsh.db_admin_assisstant.domain.query.port.in.ExecuteReadOnlyQueryUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.QueryRequest;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.QueryResultResponse;
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
public class QueryController {

    private final ExecuteReadOnlyQueryUseCase executeReadOnlyQueryUseCase;
    private final CurrentAdminResolver currentAdminResolver;

    @PostMapping("/{id}/query")
    public ApiResponse<QueryResultResponse> run(@PathVariable String id, @Valid @RequestBody QueryRequest request,
            Authentication authentication) {
        QueryResult result = executeReadOnlyQueryUseCase.execute(DatabaseId.of(id), request.sql(),
                currentAdminResolver.resolveId(authentication), currentAdminResolver.isAdmin(authentication));
        return ApiResponse.ok(QueryResultResponse.from(result));
    }
}
