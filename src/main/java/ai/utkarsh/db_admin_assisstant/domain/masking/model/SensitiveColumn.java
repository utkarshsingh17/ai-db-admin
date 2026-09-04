package ai.utkarsh.db_admin_assisstant.domain.masking.model;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;

import java.time.Instant;
import java.util.Objects;

/** A table.column pair whose values are redacted in query results unless the acting admin reveals
 * PII (see {@code QueryResultMasker}). Matching against results is by column name only — see that
 * class's javadoc for why. */
public class SensitiveColumn {

    private final SensitiveColumnId id;
    private final DatabaseId databaseId;
    private final String tableName;
    private final String columnName;
    private final Instant createdAt;

    private SensitiveColumn(SensitiveColumnId id, DatabaseId databaseId, String tableName, String columnName,
            Instant createdAt) {
        this.id = id;
        this.databaseId = databaseId;
        this.tableName = tableName;
        this.columnName = columnName;
        this.createdAt = createdAt;
    }

    public static SensitiveColumn mark(DatabaseId databaseId, String tableName, String columnName) {
        Objects.requireNonNull(databaseId, "databaseId must not be null");
        Objects.requireNonNull(tableName, "tableName must not be null");
        Objects.requireNonNull(columnName, "columnName must not be null");
        return new SensitiveColumn(SensitiveColumnId.generate(), databaseId, tableName, columnName, Instant.now());
    }

    public static SensitiveColumn reconstitute(SensitiveColumnId id, DatabaseId databaseId, String tableName,
            String columnName, Instant createdAt) {
        return new SensitiveColumn(id, databaseId, tableName, columnName, createdAt);
    }

    public SensitiveColumnId getId() {
        return id;
    }

    public DatabaseId getDatabaseId() {
        return databaseId;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
