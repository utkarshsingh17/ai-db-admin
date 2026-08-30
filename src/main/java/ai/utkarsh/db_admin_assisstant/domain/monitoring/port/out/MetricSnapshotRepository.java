package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshot;

import java.util.List;

public interface MetricSnapshotRepository {

    MetricSnapshot save(MetricSnapshot snapshot);

    List<MetricSnapshot> findRecentByDatabase(DatabaseId databaseId, int limit);

    int deleteOlderThanDays(int days);
}
