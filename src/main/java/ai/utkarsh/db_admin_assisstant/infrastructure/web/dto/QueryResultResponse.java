package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;

import java.util.List;

public record QueryResultResponse(List<String> columns, List<List<String>> rows, int rowCount, boolean truncated,
        long executionTimeMs) {

    public static QueryResultResponse from(QueryResult result) {
        return new QueryResultResponse(result.columns(), result.rows(), result.rowCount(), result.truncated(),
                result.executionTimeMs());
    }
}
