package ai.utkarsh.db_admin_assisstant.domain.schema.port.out;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.DatabaseSchema;

public interface SchemaIntrospectionPort {

    DatabaseSchema introspect(MonitoredDatabase target);
}
