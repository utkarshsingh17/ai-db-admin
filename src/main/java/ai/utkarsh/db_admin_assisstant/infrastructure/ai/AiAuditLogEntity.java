package ai.utkarsh.db_admin_assisstant.infrastructure.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Technical/cost audit of every raw LLM call — distinct from {@code AuditLogEntry}, which is the
 * business/compliance log of recommendation lifecycle actions. Written by {@link AiAuditAdvisor}.
 */
@Entity
@Table(name = "ai_audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AiAuditLogEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(nullable = false, length = 100)
    private String operation;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "input_tokens", nullable = false)
    private int inputTokens;

    @Column(name = "output_tokens", nullable = false)
    private int outputTokens;

    @Column(name = "estimated_cost_usd", nullable = false)
    private double estimatedCostUsd;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
