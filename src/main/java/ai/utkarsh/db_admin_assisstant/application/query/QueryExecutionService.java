package ai.utkarsh.db_admin_assisstant.application.query;

import ai.utkarsh.db_admin_assisstant.application.audit.AuditLogService;
import ai.utkarsh.db_admin_assisstant.application.audit.JsonPayload;
import ai.utkarsh.db_admin_assisstant.application.masking.QueryResultMasker;
import ai.utkarsh.db_admin_assisstant.application.shared.SqlStatementClassifier;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabaseNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import ai.utkarsh.db_admin_assisstant.domain.query.port.in.ExecuteReadOnlyQueryUseCase;
import ai.utkarsh.db_admin_assisstant.domain.query.port.out.ReadOnlyQueryExecutorPort;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.DraftOptimizationForQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryExecutionService implements ExecuteReadOnlyQueryUseCase {

    private final MonitoredDatabaseRepository monitoredDatabaseRepository;
    private final ReadOnlyQueryExecutorPort executor;
    private final QueryResultMasker queryResultMasker;
    private final AuditLogService auditLogService;
    private final DraftOptimizationForQueryUseCase draftOptimizationForQueryUseCase;

    @Override
    @Transactional
    public QueryResult execute(DatabaseId databaseId, String sql, UUID actingAdminId, boolean revealPii) {
        MonitoredDatabase database = monitoredDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new MonitoredDatabaseNotFoundException(databaseId));
        SqlStatementClassifier.requireReadOnly(sql);
        SqlStatementClassifier.requireSingleStatement(sql);

        QueryResult rawResult = executor.execute(database, sql);
        QueryResult result = queryResultMasker.mask(databaseId, rawResult, revealPii);

        auditLogService.record(actingAdminId.toString(), AuditAction.QUERY_EXECUTED, "MonitoredDatabase",
                databaseId.value().toString(), JsonPayload.of().put("sql", sql).put("rowCount", result.rowCount())
                        .build(),
                null);

        // Only queries actually run through the portal (this SQL editor path, and the AI_QUERY apply
        // path) ever trigger an optimization suggestion — see DraftOptimizationForQueryUseCase.
        draftOptimizationForQueryUseCase.draftIfSlow(database, sql, rawResult);
        return result;
    }
}
