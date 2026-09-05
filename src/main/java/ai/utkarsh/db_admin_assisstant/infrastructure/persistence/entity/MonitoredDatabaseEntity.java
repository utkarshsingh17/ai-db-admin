package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseEngine;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitored_database")
@Getter
@Setter
@NoArgsConstructor
public class MonitoredDatabaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DatabaseEngine engine;

    @Column(name = "jdbc_url", nullable = false, length = 500)
    private String jdbcUrl;

    @Column(nullable = false)
    private String username;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "encrypted_password", nullable = false, length = 1000)
    private String password;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "owner_admin_id", nullable = false, updatable = false)
    private UUID ownerAdminId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
