package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sensitive_column")
@Getter
@Setter
@NoArgsConstructor
public class SensitiveColumnEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "database_id", nullable = false, updatable = false)
    private UUID databaseId;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
