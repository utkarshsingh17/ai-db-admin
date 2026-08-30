package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;

public interface RequestRecommendationUseCase {

    PerformanceRecommendation requestForSlowQuery(DatabaseId databaseId, SlowQueryEventId slowQueryEventId);
}
