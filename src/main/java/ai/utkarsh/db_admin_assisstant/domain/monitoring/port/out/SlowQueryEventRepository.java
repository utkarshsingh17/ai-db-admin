package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;

import java.util.List;
import java.util.Optional;

public interface SlowQueryEventRepository {

    SlowQueryEvent save(SlowQueryEvent event);

    Optional<SlowQueryEvent> findById(SlowQueryEventId id);

    List<SlowQueryEvent> findRecentByDatabase(DatabaseId databaseId, int limit);

    int deleteOlderThanDays(int days);
}
