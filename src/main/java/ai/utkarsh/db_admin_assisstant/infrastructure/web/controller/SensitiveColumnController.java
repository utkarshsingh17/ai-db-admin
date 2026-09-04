package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumnId;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.in.ListSensitiveColumnsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.in.MarkSensitiveColumnUseCase;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.in.UnmarkSensitiveColumnUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.MarkSensitiveColumnRequest;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.SensitiveColumnResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/databases/{id}/sensitive-columns")
@RequiredArgsConstructor
public class SensitiveColumnController {

    private final MarkSensitiveColumnUseCase markUseCase;
    private final UnmarkSensitiveColumnUseCase unmarkUseCase;
    private final ListSensitiveColumnsUseCase listUseCase;
    private final CurrentAdminResolver currentAdminResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<SensitiveColumnResponse>> mark(@PathVariable String id,
            @Valid @RequestBody MarkSensitiveColumnRequest request, Authentication authentication) {
        SensitiveColumn column = markUseCase.mark(new MarkSensitiveColumnUseCase.MarkSensitiveColumnCommand(
                DatabaseId.of(id), request.tableName(), request.columnName(),
                currentAdminResolver.resolveId(authentication)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SensitiveColumnResponse.from(column)));
    }

    @GetMapping
    public ApiResponse<List<SensitiveColumnResponse>> list(@PathVariable String id) {
        return ApiResponse.ok(listUseCase.listForDatabase(DatabaseId.of(id)).stream()
                .map(SensitiveColumnResponse::from).toList());
    }

    @DeleteMapping("/{columnId}")
    public ApiResponse<Void> unmark(@PathVariable String id, @PathVariable String columnId,
            Authentication authentication) {
        unmarkUseCase.unmark(SensitiveColumnId.of(columnId), currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(null);
    }
}
