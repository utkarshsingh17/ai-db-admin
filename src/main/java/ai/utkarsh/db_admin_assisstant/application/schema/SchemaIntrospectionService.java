package ai.utkarsh.db_admin_assisstant.application.schema;

import ai.utkarsh.db_admin_assisstant.application.audit.AuditLogService;
import ai.utkarsh.db_admin_assisstant.domain.audit.model.AuditAction;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabaseNotFoundException;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.port.out.MonitoredDatabaseRepository;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.DatabaseSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.port.in.IntrospectDatabaseSchemaUseCase;
import ai.utkarsh.db_admin_assisstant.domain.schema.port.out.SchemaIntrospectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchemaIntrospectionService implements IntrospectDatabaseSchemaUseCase {

    private final MonitoredDatabaseRepository monitoredDatabaseRepository;
    private final SchemaIntrospectionPort introspectionPort;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public DatabaseSchema introspect(DatabaseId databaseId, UUID actingAdminId) {
        MonitoredDatabase database = monitoredDatabaseRepository.findById(databaseId)
                .orElseThrow(() -> new MonitoredDatabaseNotFoundException(databaseId));
        DatabaseSchema schema = introspectionPort.introspect(database);
        auditLogService.record(actingAdminId.toString(), AuditAction.SCHEMA_VIEWED, "MonitoredDatabase",
                databaseId.value().toString(), null, null);
        return schema;
    }
}
