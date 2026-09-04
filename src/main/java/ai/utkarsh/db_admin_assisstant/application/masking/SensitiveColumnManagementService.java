package ai.utkarsh.db_admin_assisstant.application.masking;

import ai.utkarsh.db_admin_assisstant.application.audit.AuditLogService;
import ai.utkarsh.db_admin_assisstant.application.audit.JsonPayload;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumnId;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumnNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.in.ListSensitiveColumnsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.in.MarkSensitiveColumnUseCase;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.in.UnmarkSensitiveColumnUseCase;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.out.SensitiveColumnRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SensitiveColumnManagementService
        implements MarkSensitiveColumnUseCase, UnmarkSensitiveColumnUseCase, ListSensitiveColumnsUseCase {

    private final SensitiveColumnRepository repository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SensitiveColumn mark(MarkSensitiveColumnCommand command) {
        if (repository.existsByDatabaseIdAndTableNameAndColumnName(command.databaseId(), command.tableName(),
                command.columnName())) {
            throw new IllegalArgumentException(
                    command.tableName() + "." + command.columnName() + " is already marked sensitive");
        }
        SensitiveColumn column = SensitiveColumn.mark(command.databaseId(), command.tableName(),
                command.columnName());
        SensitiveColumn saved = repository.save(column);
        auditLogService.record(command.actingAdminId().toString(), AuditAction.SENSITIVE_COLUMN_MARKED,
                "SensitiveColumn", saved.getId().value().toString(),
                JsonPayload.of().put("table", saved.getTableName()).put("column", saved.getColumnName()).build(),
                null);
        return saved;
    }

    @Override
    @Transactional
    public void unmark(SensitiveColumnId id, UUID actingAdminId) {
        SensitiveColumn column = repository.findById(id).orElseThrow(() -> new SensitiveColumnNotFoundException(id));
        repository.deleteById(id);
        auditLogService.record(actingAdminId.toString(), AuditAction.SENSITIVE_COLUMN_UNMARKED, "SensitiveColumn",
                id.value().toString(),
                JsonPayload.of().put("table", column.getTableName()).put("column", column.getColumnName()).build(),
                null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensitiveColumn> listForDatabase(DatabaseId databaseId) {
        return repository.findByDatabaseId(databaseId);
    }
}
