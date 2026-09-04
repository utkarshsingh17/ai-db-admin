package ai.utkarsh.db_admin_assisstant.infrastructure.scheduling;

import ai.utkarsh.db_admin_assisstant.application.monitoring.RetentionCleanupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * No periodic database polling here on purpose — metrics/slow-query analysis used to come from a
 * background scan of every monitored database ({@code pg_stat_statements}, {@code pg_stat_activity},
 * etc.), which meant the AI could "see" and react to traffic that never went through this app.
 * Recommendations are now drafted only for queries actually run through the portal — see
 * {@code DraftOptimizationForQueryUseCase}. Retention cleanup is unrelated housekeeping (purges the
 * app's own old records) and stays scheduled.
 */
@Component
public class MonitoringScheduler {

    private final RetentionCleanupService retentionCleanupService;
    private final int retentionDays;

    public MonitoringScheduler(RetentionCleanupService retentionCleanupService,
            @Value("${app.monitoring.retention-days}") int retentionDays) {
        this.retentionCleanupService = retentionCleanupService;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeOldData() {
        retentionCleanupService.purgeOlderThan(retentionDays);
    }
}
