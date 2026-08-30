package ai.utkarsh.db_admin_assisstant.application.monitoring;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MetricSnapshotRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.SlowQueryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionCleanupService {

    private final MetricSnapshotRepository metricSnapshotRepository;
    private final SlowQueryEventRepository slowQueryEventRepository;

    @Transactional
    public void purgeOlderThan(int days) {
        int snapshots = metricSnapshotRepository.deleteOlderThanDays(days);
        int events = slowQueryEventRepository.deleteOlderThanDays(days);
        log.info("Retention cleanup removed {} metric snapshots and {} slow query events older than {} days",
                snapshots, events, days);
    }
}
