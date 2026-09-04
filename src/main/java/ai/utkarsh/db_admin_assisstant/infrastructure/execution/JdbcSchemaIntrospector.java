package ai.utkarsh.db_admin_assisstant.infrastructure.execution;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.MonitoredDatabase;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.ColumnSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.DatabaseSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.ForeignKeySchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.model.TableSchema;
import ai.utkarsh.db_admin_assisstant.domain.schema.port.out.SchemaIntrospectionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Introspects a monitored database's own {@code public} schema via plain {@link DatabaseMetaData}
 * — no engine-specific SQL needed, so this works unchanged if a non-Postgres engine is ever added.
 * Same short-lived connection pattern as the other JDBC adapters in this package.
 */
@Component
@Slf4j
public class JdbcSchemaIntrospector implements SchemaIntrospectionPort {

    private static final String SCHEMA = "public";

    @Override
    public DatabaseSchema introspect(MonitoredDatabase target) {
        try (Connection connection = DriverManager.getConnection(target.getJdbcUrl(), target.getUsername(),
                target.getPassword())) {
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> tableNames = readTableNames(metaData);

            List<TableSchema> tables = new ArrayList<>();
            List<ForeignKeySchema> foreignKeys = new ArrayList<>();
            for (String tableName : tableNames) {
                Set<String> primaryKeyColumns = readPrimaryKeyColumns(metaData, tableName);
                tables.add(new TableSchema(tableName, readColumns(metaData, tableName, primaryKeyColumns)));
                foreignKeys.addAll(readForeignKeys(metaData, tableName));
            }
            return new DatabaseSchema(tables, foreignKeys);
        } catch (SQLException e) {
            log.error("Failed to introspect schema for {}: {}", target.getName(), e.getMessage());
            throw new IllegalArgumentException("Failed to introspect schema: " + e.getMessage());
        }
    }

    private List<String> readTableNames(DatabaseMetaData metaData) throws SQLException {
        List<String> names = new ArrayList<>();
        try (ResultSet rs = metaData.getTables(null, SCHEMA, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }
        return names;
    }

    private Set<String> readPrimaryKeyColumns(DatabaseMetaData metaData, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = metaData.getPrimaryKeys(null, SCHEMA, tableName)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }

    private List<ColumnSchema> readColumns(DatabaseMetaData metaData, String tableName,
            Set<String> primaryKeyColumns) throws SQLException {
        List<ColumnSchema> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(null, SCHEMA, tableName, "%")) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                columns.add(new ColumnSchema(name, rs.getString("TYPE_NAME"),
                        "YES".equals(rs.getString("IS_NULLABLE")), primaryKeyColumns.contains(name)));
            }
        }
        return columns;
    }

    private List<ForeignKeySchema> readForeignKeys(DatabaseMetaData metaData, String tableName) throws SQLException {
        List<ForeignKeySchema> foreignKeys = new ArrayList<>();
        try (ResultSet rs = metaData.getImportedKeys(null, SCHEMA, tableName)) {
            while (rs.next()) {
                foreignKeys.add(new ForeignKeySchema(rs.getString("FKTABLE_NAME"), rs.getString("FKCOLUMN_NAME"),
                        rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME")));
            }
        }
        return foreignKeys;
    }
}
