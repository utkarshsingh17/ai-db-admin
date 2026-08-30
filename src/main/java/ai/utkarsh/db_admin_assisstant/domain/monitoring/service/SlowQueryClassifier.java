package ai.utkarsh.db_admin_assisstant.domain.monitoring.service;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQuerySeverity;

/** Pure domain logic: decides how severe a captured slow query is, given a configured threshold. */
public final class SlowQueryClassifier {

    private final double slowThresholdMs;
    private final double severeMultiplier;

    public SlowQueryClassifier(double slowThresholdMs) {
        this(slowThresholdMs, 5.0);
    }

    public SlowQueryClassifier(double slowThresholdMs, double severeMultiplier) {
        this.slowThresholdMs = slowThresholdMs;
        this.severeMultiplier = severeMultiplier;
    }

    public SlowQuerySeverity classify(SlowQueryEvent event) {
        if (!event.exceedsThreshold(slowThresholdMs)) {
            return SlowQuerySeverity.NORMAL;
        }
        if (event.getMeanExecTimeMs() >= slowThresholdMs * severeMultiplier) {
            return SlowQuerySeverity.SEVERE;
        }
        return SlowQuerySeverity.MODERATE;
    }
}
