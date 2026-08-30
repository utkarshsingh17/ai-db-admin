package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;

import java.util.List;

public interface ListSlowQueriesUseCase {

    List<SlowQueryEvent> listRecent(DatabaseId databaseId, int limit);
}
