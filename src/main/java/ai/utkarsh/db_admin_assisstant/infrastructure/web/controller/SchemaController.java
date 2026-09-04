package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.schema.port.in.IntrospectDatabaseSchemaUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.DatabaseSchemaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/databases")
@RequiredArgsConstructor
public class SchemaController {

    private final IntrospectDatabaseSchemaUseCase introspectDatabaseSchemaUseCase;
    private final CurrentAdminResolver currentAdminResolver;

    @GetMapping("/{id}/schema")
    public ApiResponse<DatabaseSchemaResponse> schema(@PathVariable String id, Authentication authentication) {
        return ApiResponse.ok(DatabaseSchemaResponse.from(
                introspectDatabaseSchemaUseCase.introspect(DatabaseId.of(id),
                        currentAdminResolver.resolveId(authentication))));
    }
}
