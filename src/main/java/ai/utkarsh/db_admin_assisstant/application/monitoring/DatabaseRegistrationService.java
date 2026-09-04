package ai.utkarsh.db_admin_assisstant.application.monitoring;

import ai.utkarsh.db_admin_assisstant.application.audit.AuditLogService;
import ai.utkarsh.db_admin_assisstant.application.audit.JsonPayload;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabaseNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.DeleteMonitoredDatabaseUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.ListMonitoredDatabasesUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.RegisterMonitoredDatabaseUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in.SetMonitoredDatabaseEnabledUseCase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DatabaseRegistrationService implements RegisterMonitoredDatabaseUseCase, ListMonitoredDatabasesUseCase,
        SetMonitoredDatabaseEnabledUseCase, DeleteMonitoredDatabaseUseCase {

    private final MonitoredDatabaseRepository repository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public MonitoredDatabase register(RegisterDatabaseCommand command) {
        if (repository.existsByName(command.name())) {
            throw new IllegalArgumentException("A monitored database named '" + command.name() + "' already exists");
        }
        MonitoredDatabase database = MonitoredDatabase.register(command.name(), command.engine(), command.jdbcUrl(),
                command.username(), command.password());
        MonitoredDatabase saved = repository.save(database);
        auditLogService.record("SYSTEM", AuditAction.DATABASE_REGISTERED, "MonitoredDatabase",
                saved.getId().value().toString(), JsonPayload.of().put("name", saved.getName()).build(), null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonitoredDatabase> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public MonitoredDatabase setEnabled(DatabaseId id, boolean enabled, UUID adminUserId) {
        MonitoredDatabase database = repository.findById(id)
                .orElseThrow(() -> new MonitoredDatabaseNotFoundException(id));
        if (enabled) {
            database.enable();
        } else {
            database.disable();
        }
        MonitoredDatabase saved = repository.save(database);
        auditLogService.record(adminUserId.toString(),
                enabled ? AuditAction.DATABASE_ENABLED : AuditAction.DATABASE_DISABLED, "MonitoredDatabase",
                saved.getId().value().toString(), JsonPayload.of().put("name", saved.getName()).build(), null);
        return saved;
    }

    @Override
    @Transactional
    public void delete(DatabaseId id, UUID adminUserId) {
        MonitoredDatabase database = repository.findById(id)
                .orElseThrow(() -> new MonitoredDatabaseNotFoundException(id));
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Cannot delete '" + database.getName()
                    + "' — it has existing metrics, slow queries, or recommendations. Disable it instead.");
        }
        auditLogService.record(adminUserId.toString(), AuditAction.DATABASE_DELETED, "MonitoredDatabase",
                id.value().toString(), JsonPayload.of().put("name", database.getName()).build(), null);
    }
}
