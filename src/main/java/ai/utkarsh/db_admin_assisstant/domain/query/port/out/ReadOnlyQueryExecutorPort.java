package ai.utkarsh.db_admin_assisstant.domain.query.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;

public interface ReadOnlyQueryExecutorPort {

    QueryResult execute(MonitoredDatabase target, String sql);
}
