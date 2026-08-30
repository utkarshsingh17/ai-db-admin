package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.ListSlowQueriesUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.SlowQueryEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slow-queries")
@RequiredArgsConstructor
public class SlowQueryController {

    private final ListSlowQueriesUseCase listSlowQueriesUseCase;

    @GetMapping
    public ApiResponse<List<SlowQueryEventResponse>> list(@RequestParam String databaseId,
            @RequestParam(defaultValue = "50") int limit) {
        List<SlowQueryEventResponse> response = listSlowQueriesUseCase
                .listRecent(DatabaseId.of(databaseId), Math.min(limit, 200)).stream()
                .map(SlowQueryEventResponse::from).toList();
        return ApiResponse.ok(response);
    }
}
