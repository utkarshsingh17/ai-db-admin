package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

/**
 * Lifecycle: DRAFT -&gt; PENDING_APPROVAL -&gt; (APPROVED | REJECTED); APPROVED -&gt; APPLYING -&gt;
 * (APPLIED | ALREADY_EXISTS | FAILED). ALREADY_EXISTS covers a DDL statement whose target (index,
 * table, etc.) already exists on the monitored database — the desired end state is already true,
 * so it is a resolved outcome, not a failure. Transitions are enforced by
 * {@link PerformanceRecommendation}, never by callers.
 */
public enum RecommendationStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    APPLYING,
    APPLIED,
    ALREADY_EXISTS,
    FAILED
}
