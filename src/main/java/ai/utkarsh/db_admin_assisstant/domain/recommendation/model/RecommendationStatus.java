package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

/**
 * Lifecycle: DRAFT -&gt; PENDING_APPROVAL -&gt; (APPROVED | REJECTED); APPROVED -&gt; APPLYING -&gt; (APPLIED | FAILED).
 * Transitions are enforced by {@link PerformanceRecommendation}, never by callers.
 */
public enum RecommendationStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    APPLYING,
    APPLIED,
    FAILED
}
