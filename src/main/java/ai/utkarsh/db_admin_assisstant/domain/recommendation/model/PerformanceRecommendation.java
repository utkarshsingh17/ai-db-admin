package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEventId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationAlreadyExistsEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationAppliedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationApplyFailedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationApprovedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationCreatedEvent;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.event.RecommendationRejectedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root. Every state transition is enforced here — application services orchestrate but
 * never decide whether a transition is legal. Domain events are collected on each transition and
 * drained by {@link #pullDomainEvents()} after a successful save, per the DDD publish-after-commit
 * pattern; the audit log is built entirely from these events.
 */
public class PerformanceRecommendation {

    private final RecommendationId id;
    private final DatabaseId databaseId;
    private final SlowQueryEventId slowQueryEventId; // nullable — config-change recs may not trace to one query
    private final RecommendationType type;
    private RecommendationStatus status;
    private final RiskLevel riskLevel;
    private final String title;
    private final String explanation;
    private final Sql proposedSql;
    private final String targetObject; // nullable
    private String failureReason;
    private Instant appliedAt;
    private final List<ApprovalDecision> approvalDecisions = new ArrayList<>();
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<Object> domainEvents = new ArrayList<>();

    private PerformanceRecommendation(RecommendationId id, DatabaseId databaseId, SlowQueryEventId slowQueryEventId,
            RecommendationType type, RecommendationStatus status, RiskLevel riskLevel, String title,
            String explanation, Sql proposedSql, String targetObject, String failureReason, Instant appliedAt,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.databaseId = databaseId;
        this.slowQueryEventId = slowQueryEventId;
        this.type = type;
        this.status = status;
        this.riskLevel = riskLevel;
        this.title = title;
        this.explanation = explanation;
        this.proposedSql = proposedSql;
        this.targetObject = targetObject;
        this.failureReason = failureReason;
        this.appliedAt = appliedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PerformanceRecommendation draft(DatabaseId databaseId, SlowQueryEventId slowQueryEventId,
            RecommendationType type, RiskLevel riskLevel, String title, String explanation, Sql proposedSql,
            String targetObject) {
        Objects.requireNonNull(databaseId, "databaseId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        Objects.requireNonNull(proposedSql, "proposedSql must not be null");
        Instant now = Instant.now();
        return new PerformanceRecommendation(RecommendationId.generate(), databaseId, slowQueryEventId, type,
                RecommendationStatus.DRAFT, riskLevel, title, explanation, proposedSql, targetObject, null, null,
                now, now);
    }

    public static PerformanceRecommendation reconstitute(RecommendationId id, DatabaseId databaseId,
            SlowQueryEventId slowQueryEventId, RecommendationType type, RecommendationStatus status,
            RiskLevel riskLevel, String title, String explanation, Sql proposedSql, String targetObject,
            String failureReason, Instant appliedAt, List<ApprovalDecision> approvalDecisions, Instant createdAt,
            Instant updatedAt) {
        PerformanceRecommendation recommendation = new PerformanceRecommendation(id, databaseId, slowQueryEventId,
                type, status, riskLevel, title, explanation, proposedSql, targetObject, failureReason, appliedAt,
                createdAt, updatedAt);
        recommendation.approvalDecisions.addAll(approvalDecisions);
        return recommendation;
    }

    public void submitForApproval() {
        requireStatus(RecommendationStatus.DRAFT, "submit for approval");
        this.status = RecommendationStatus.PENDING_APPROVAL;
        touch();
        domainEvents.add(new RecommendationCreatedEvent(id, databaseId, type, Instant.now()));
    }

    public void approve(UUID adminUserId, String comment) {
        requireStatus(RecommendationStatus.PENDING_APPROVAL, "approve");
        this.status = RecommendationStatus.APPROVED;
        approvalDecisions.add(ApprovalDecision.approve(adminUserId, comment));
        touch();
        domainEvents.add(new RecommendationApprovedEvent(id, adminUserId, Instant.now()));
    }

    public void reject(UUID adminUserId, String reason) {
        requireStatus(RecommendationStatus.PENDING_APPROVAL, "reject");
        this.status = RecommendationStatus.REJECTED;
        approvalDecisions.add(ApprovalDecision.reject(adminUserId, reason));
        touch();
        domainEvents.add(new RecommendationRejectedEvent(id, adminUserId, reason, Instant.now()));
    }

    public void startApplying() {
        requireStatus(RecommendationStatus.APPROVED, "apply");
        this.status = RecommendationStatus.APPLYING;
        touch();
    }

    public void markApplied(UUID appliedByAdminUserId) {
        requireStatus(RecommendationStatus.APPLYING, "mark applied");
        this.status = RecommendationStatus.APPLIED;
        this.appliedAt = Instant.now();
        touch();
        domainEvents.add(new RecommendationAppliedEvent(id, appliedByAdminUserId, Instant.now()));
    }

    /** The DDL's target (index, table, etc.) already existed — the desired end state is already
     * true, so this is a resolved outcome, not a failure. */
    public void markAlreadyExists(UUID appliedByAdminUserId) {
        requireStatus(RecommendationStatus.APPLYING, "mark already exists");
        this.status = RecommendationStatus.ALREADY_EXISTS;
        this.appliedAt = Instant.now();
        touch();
        domainEvents.add(new RecommendationAlreadyExistsEvent(id, appliedByAdminUserId, Instant.now()));
    }

    public void markFailed(UUID appliedByAdminUserId, String reason) {
        requireStatus(RecommendationStatus.APPLYING, "mark failed");
        this.status = RecommendationStatus.FAILED;
        this.failureReason = reason;
        touch();
        domainEvents.add(new RecommendationApplyFailedEvent(id, appliedByAdminUserId, reason, Instant.now()));
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private void requireStatus(RecommendationStatus required, String action) {
        if (status != required) {
            throw new InvalidRecommendationStateException(id, status, action);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public RecommendationId getId() {
        return id;
    }

    public DatabaseId getDatabaseId() {
        return databaseId;
    }

    public SlowQueryEventId getSlowQueryEventId() {
        return slowQueryEventId;
    }

    public RecommendationType getType() {
        return type;
    }

    public RecommendationStatus getStatus() {
        return status;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getTitle() {
        return title;
    }

    public String getExplanation() {
        return explanation;
    }

    public Sql getProposedSql() {
        return proposedSql;
    }

    public String getTargetObject() {
        return targetObject;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public List<ApprovalDecision> getApprovalDecisions() {
        return List.copyOf(approvalDecisions);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
