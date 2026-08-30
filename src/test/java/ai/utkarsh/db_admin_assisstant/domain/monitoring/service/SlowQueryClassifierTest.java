package ai.utkarsh.db_admin_assisstant.domain.monitoring.service;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.QueryFingerprint;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQuerySeverity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SlowQueryClassifierTest {

    private final SlowQueryClassifier classifier = new SlowQueryClassifier(500.0);

    private SlowQueryEvent eventWithMeanTime(double meanExecTimeMs) {
        return SlowQueryEvent.capture(DatabaseId.generate(), new QueryFingerprint("abc123"), "SELECT 1", 10,
                meanExecTimeMs, meanExecTimeMs * 10, 1L, Instant.now());
    }

    @Test
    void belowThreshold_isNormal() {
        assertThat(classifier.classify(eventWithMeanTime(100))).isEqualTo(SlowQuerySeverity.NORMAL);
    }

    @Test
    void aboveThreshold_isModerate() {
        assertThat(classifier.classify(eventWithMeanTime(600))).isEqualTo(SlowQuerySeverity.MODERATE);
    }

    @Test
    void wayAboveThreshold_isSevere() {
        assertThat(classifier.classify(eventWithMeanTime(3000))).isEqualTo(SlowQuerySeverity.SEVERE);
    }
}
