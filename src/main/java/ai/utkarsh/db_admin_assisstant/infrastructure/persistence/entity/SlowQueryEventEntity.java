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
@Table(name = "slow_query_event")
@Getter
@Setter
@NoArgsConstructor
public class SlowQueryEventEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "database_id", nullable = false, updatable = false)
    private UUID databaseId;

    @Column(name = "query_fingerprint", nullable = false, length = 64)
    private String queryFingerprint;

    @Column(name = "normalized_query", nullable = false, columnDefinition = "TEXT")
    private String normalizedQuery;

    @Column(nullable = false)
    private long calls;

    @Column(name = "mean_exec_time_ms", nullable = false)
    private double meanExecTimeMs;

    @Column(name = "total_exec_time_ms", nullable = false)
    private double totalExecTimeMs;

    @Column(name = "rows_returned")
    private Long rowsReturned;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
