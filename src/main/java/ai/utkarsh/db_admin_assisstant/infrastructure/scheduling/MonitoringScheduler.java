package ai.utkarsh.db_admin_assisstant.infrastructure.scheduling;

import ai.utkarsh.db_admin_assisstant.application.monitoring.RetentionCleanupService;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.CollectMetricsUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitoringScheduler {

    private final CollectMetricsUseCase collectMetricsUseCase;
    private final RetentionCleanupService retentionCleanupService;
    private final int retentionDays;

    public MonitoringScheduler(CollectMetricsUseCase collectMetricsUseCase,
            RetentionCleanupService retentionCleanupService,
            @Value("${app.monitoring.retention-days}") int retentionDays) {
        this.collectMetricsUseCase = collectMetricsUseCase;
        this.retentionCleanupService = retentionCleanupService;
        this.retentionDays = retentionDays;
    }

    @Scheduled(fixedDelayString = "${app.monitoring.poll-interval-ms}")
    public void pollMonitoredDatabases() {
        collectMetricsUseCase.collectAll();
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeOldData() {
        retentionCleanupService.purgeOlderThan(retentionDays);
    }
}
