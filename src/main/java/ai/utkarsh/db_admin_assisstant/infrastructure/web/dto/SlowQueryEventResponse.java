package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;

import java.time.Instant;
import java.util.UUID;

public record SlowQueryEventResponse(UUID id, String normalizedQuery, long calls, double meanExecTimeMs,
        double totalExecTimeMs, Instant capturedAt) {

    public static SlowQueryEventResponse from(SlowQueryEvent event) {
        return new SlowQueryEventResponse(event.getId().value(), event.getNormalizedQuery(), event.getCalls(),
                event.getMeanExecTimeMs(), event.getTotalExecTimeMs(), event.getCapturedAt());
    }
}
