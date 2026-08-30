package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.ListMetricSnapshotsUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.MetricSnapshotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final ListMetricSnapshotsUseCase listMetricSnapshotsUseCase;

    @GetMapping("/{databaseId}")
    public ApiResponse<List<MetricSnapshotResponse>> list(@PathVariable String databaseId,
            @RequestParam(defaultValue = "50") int limit) {
        List<MetricSnapshotResponse> response = listMetricSnapshotsUseCase
                .listRecent(DatabaseId.of(databaseId), Math.min(limit, 200)).stream()
                .map(MetricSnapshotResponse::from).toList();
        return ApiResponse.ok(response);
    }
}
