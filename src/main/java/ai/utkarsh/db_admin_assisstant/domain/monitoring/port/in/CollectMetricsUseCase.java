package ai.utkarsh.db_admin_assisstant.domain.monitoring.port.in;

public interface CollectMetricsUseCase {

    /** Polls every enabled monitored database once and persists a snapshot + any newly slow queries. */
    void collectAll();
}
