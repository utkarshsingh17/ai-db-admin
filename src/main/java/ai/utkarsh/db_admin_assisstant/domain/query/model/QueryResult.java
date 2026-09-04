package ai.utkarsh.db_admin_assisstant.domain.query.model;

import java.util.List;

public record QueryResult(List<String> columns, List<List<String>> rows, int rowCount, boolean truncated,
        long executionTimeMs) {
}
