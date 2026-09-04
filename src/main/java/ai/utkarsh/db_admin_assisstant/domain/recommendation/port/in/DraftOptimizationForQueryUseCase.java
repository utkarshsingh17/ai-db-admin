package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;

/**
 * Best-effort optimization suggestion for a query that was actually run through the portal (the SQL
 * editor's "Write SQL" tab, or an applied {@code AI_QUERY} recommendation) — as opposed to the
 * previous behavior of scanning every query PostgreSQL's {@code pg_stat_statements} sees regardless
 * of who ran it. Returns null when the query wasn't slow enough to warrant a suggestion, or when
 * drafting failed (AI drafting failures must never fail the query execution itself).
 */
public interface DraftOptimizationForQueryUseCase {

    RecommendationId draftIfSlow(MonitoredDatabase target, String sql, QueryResult result);
}
