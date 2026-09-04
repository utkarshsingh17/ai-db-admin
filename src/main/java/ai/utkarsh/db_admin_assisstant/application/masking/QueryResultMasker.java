package ai.utkarsh.db_admin_assisstant.application.masking;

import ai.utkarsh.db_admin_assisstant.application.shared.PiiMasker;
import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumn;
import ai.utkarsh.db_admin_assisstant.domain.masking.port.out.SensitiveColumnRepository;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.query.model.QueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redacts values in sensitive columns before a {@link QueryResult} is returned to the caller.
 * Matching is by column name only (case-insensitive), not table-qualified — {@code
 * ResultSetMetaData}'s reported table name is unreliable for aliased or computed columns, and "hide
 * email everywhere" is the intuitive behavior for an admin marking a column sensitive anyway.
 */
@Component
@RequiredArgsConstructor
public class QueryResultMasker {

    private final SensitiveColumnRepository sensitiveColumnRepository;

    public QueryResult mask(DatabaseId databaseId, QueryResult result, boolean revealPii) {
        if (revealPii) {
            return result;
        }
        Set<String> sensitiveNames = sensitiveColumnRepository.findByDatabaseId(databaseId).stream()
                .map(SensitiveColumn::getColumnName)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (sensitiveNames.isEmpty()) {
            return result;
        }

        boolean[] maskColumn = new boolean[result.columns().size()];
        for (int i = 0; i < result.columns().size(); i++) {
            maskColumn[i] = sensitiveNames.contains(result.columns().get(i).toLowerCase(Locale.ROOT));
        }

        List<List<String>> maskedRows = new ArrayList<>(result.rows().size());
        for (List<String> row : result.rows()) {
            List<String> maskedRow = new ArrayList<>(row.size());
            for (int i = 0; i < row.size(); i++) {
                maskedRow.add(maskColumn[i] ? PiiMasker.mask(result.columns().get(i), row.get(i)) : row.get(i));
            }
            maskedRows.add(maskedRow);
        }
        return new QueryResult(result.columns(), maskedRows, result.rowCount(), result.truncated(),
                result.executionTimeMs());
    }
}
