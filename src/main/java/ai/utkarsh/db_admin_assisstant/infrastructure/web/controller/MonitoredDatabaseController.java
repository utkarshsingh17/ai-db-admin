package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseEngine;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.DeleteMonitoredDatabaseUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.ListMonitoredDatabasesUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.RegisterMonitoredDatabaseUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.SetMonitoredDatabaseEnabledUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.MonitoredDatabaseResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.RegisterDatabaseRequest;
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
@RequestMapping("/api/v1/monitored-databases")
@RequiredArgsConstructor
public class MonitoredDatabaseController {

    private final RegisterMonitoredDatabaseUseCase registerUseCase;
    private final ListMonitoredDatabasesUseCase listUseCase;
    private final SetMonitoredDatabaseEnabledUseCase setEnabledUseCase;
    private final DeleteMonitoredDatabaseUseCase deleteUseCase;
    private final CurrentAdminResolver currentAdminResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<MonitoredDatabaseResponse>> register(
            @Valid @RequestBody RegisterDatabaseRequest request, Authentication authentication) {
        MonitoredDatabase database = registerUseCase.register(
                new RegisterMonitoredDatabaseUseCase.RegisterDatabaseCommand(request.name(), DatabaseEngine.POSTGRESQL,
                        request.jdbcUrl(), request.username(), request.password(),
                        currentAdminResolver.resolveId(authentication)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(MonitoredDatabaseResponse.from(database)));
    }

    @GetMapping
    public ApiResponse<List<MonitoredDatabaseResponse>> list(Authentication authentication) {
        return ApiResponse.ok(listUseCase.listVisibleTo(currentAdminResolver.resolveId(authentication)).stream()
                .map(MonitoredDatabaseResponse::from).toList());
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<MonitoredDatabaseResponse> disable(@PathVariable String id, Authentication authentication) {
        MonitoredDatabase database = setEnabledUseCase.setEnabled(DatabaseId.of(id), false,
                currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(MonitoredDatabaseResponse.from(database));
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<MonitoredDatabaseResponse> enable(@PathVariable String id, Authentication authentication) {
        MonitoredDatabase database = setEnabledUseCase.setEnabled(DatabaseId.of(id), true,
                currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(MonitoredDatabaseResponse.from(database));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id, Authentication authentication) {
        deleteUseCase.delete(DatabaseId.of(id), currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(null);
    }
}
