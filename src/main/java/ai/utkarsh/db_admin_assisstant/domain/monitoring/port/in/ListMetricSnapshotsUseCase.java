package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MetricSnapshot;

import java.util.List;

public interface ListMetricSnapshotsUseCase {

    List<MetricSnapshot> listRecent(DatabaseId databaseId, int limit);
}
