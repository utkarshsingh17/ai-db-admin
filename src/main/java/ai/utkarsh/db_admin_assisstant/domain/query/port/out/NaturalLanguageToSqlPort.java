package ai.utkarsh.db_admin_assisstant.domain.query.port.out;

public interface NaturalLanguageToSqlPort {

    GeneratedSql translate(String databaseName, String schemaSummary, String question);
}
