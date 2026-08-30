package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "metric_snapshot", indexes = {
        @Index(name = "idx_metric_snapshot_database_captured", columnList = "database_id, captured_at") })
@Getter
@Setter
@NoArgsConstructor
public class MetricSnapshotEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "database_id", nullable = false, updatable = false)
    private UUID databaseId;

    @Column(name = "active_connections")
    private Integer activeConnections;

    @Column(name = "max_connections")
    private Integer maxConnections;

    @Column(name = "cache_hit_ratio")
    private Double cacheHitRatio;

    @Column(name = "lock_wait_count")
    private Integer lockWaitCount;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
