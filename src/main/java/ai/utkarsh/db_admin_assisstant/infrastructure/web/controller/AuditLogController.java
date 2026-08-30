package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.audit.port.in.ListAuditLogUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.AuditLogEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

    private final ListAuditLogUseCase listAuditLogUseCase;

    @GetMapping
    public ApiResponse<List<AuditLogEntryResponse>> list(@RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId, @RequestParam(defaultValue = "50") int limit) {
        int cappedLimit = Math.min(limit, 200);
        List<AuditLogEntryResponse> response = (entityType != null && entityId != null
                ? listAuditLogUseCase.listByEntity(entityType, entityId, cappedLimit)
                : listAuditLogUseCase.listRecent(cappedLimit)).stream().map(AuditLogEntryResponse::from).toList();
        return ApiResponse.ok(response);
    }
}
