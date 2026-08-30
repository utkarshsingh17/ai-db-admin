package ai.utkarsh.db_admin_assisstant.application.monitoring;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.ListSlowQueriesUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.SlowQueryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlowQueryQueryService implements ListSlowQueriesUseCase {

    private final SlowQueryEventRepository slowQueryEventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SlowQueryEvent> listRecent(DatabaseId databaseId, int limit) {
        return slowQueryEventRepository.findRecentByDatabase(databaseId, limit);
    }
}
