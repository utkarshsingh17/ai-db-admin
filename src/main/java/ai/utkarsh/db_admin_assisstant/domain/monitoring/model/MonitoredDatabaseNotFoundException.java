package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import ai.utkarsh.db_admin_assisstant.domain.shared.DomainException;

public class MonitoredDatabaseNotFoundException extends DomainException {

    public MonitoredDatabaseNotFoundException(DatabaseId id) {
        super("MONITORED_DATABASE_NOT_FOUND", "No monitored database found with id: " + id.value());
    }
}
