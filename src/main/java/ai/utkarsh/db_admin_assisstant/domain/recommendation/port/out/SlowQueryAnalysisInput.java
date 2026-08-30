package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out;

/** Context handed to the AI adapter for one slow query it is asked to explain and recommend on. */
public record SlowQueryAnalysisInput(
        String databaseName,
        String normalizedQuery,
        long calls,
        double meanExecTimeMs,
        double totalExecTimeMs) {
}
