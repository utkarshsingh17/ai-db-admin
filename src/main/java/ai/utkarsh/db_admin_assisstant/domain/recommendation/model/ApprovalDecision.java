package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Child entity of {@link PerformanceRecommendation} — who decided what, when, and why. */
public class ApprovalDecision {

    private final UUID id;
    private final ApprovalDecisionType decision;
    private final UUID adminUserId;
    private final String comment;
    private final Instant decidedAt;

    private ApprovalDecision(UUID id, ApprovalDecisionType decision, UUID adminUserId, String comment,
            Instant decidedAt) {
        this.id = id;
        this.decision = decision;
        this.adminUserId = adminUserId;
        this.comment = comment;
        this.decidedAt = decidedAt;
    }

    public static ApprovalDecision approve(UUID adminUserId, String comment) {
        return new ApprovalDecision(UUID.randomUUID(), ApprovalDecisionType.APPROVED,
                Objects.requireNonNull(adminUserId, "adminUserId must not be null"), comment, Instant.now());
    }

    public static ApprovalDecision reject(UUID adminUserId, String comment) {
        return new ApprovalDecision(UUID.randomUUID(), ApprovalDecisionType.REJECTED,
                Objects.requireNonNull(adminUserId, "adminUserId must not be null"), comment, Instant.now());
    }

    public static ApprovalDecision reconstitute(UUID id, ApprovalDecisionType decision, UUID adminUserId,
            String comment, Instant decidedAt) {
        return new ApprovalDecision(id, decision, adminUserId, comment, decidedAt);
    }

    public UUID getId() {
        return id;
    }

    public ApprovalDecisionType getDecision() {
        return decision;
    }

    public UUID getAdminUserId() {
        return adminUserId;
    }

    public String getComment() {
        return comment;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
