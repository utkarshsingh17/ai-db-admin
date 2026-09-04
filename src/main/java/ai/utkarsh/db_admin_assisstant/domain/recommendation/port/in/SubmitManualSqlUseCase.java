package ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RiskLevel;

import java.util.UUID;

public interface SubmitManualSqlUseCase {

    PerformanceRecommendation submitManualSql(SubmitManualSqlCommand command);

    record SubmitManualSqlCommand(DatabaseId databaseId, String title, String explanation, String proposedSql,
            RiskLevel riskLevel, String targetObject, UUID submittedByAdminId) {
    }
}
