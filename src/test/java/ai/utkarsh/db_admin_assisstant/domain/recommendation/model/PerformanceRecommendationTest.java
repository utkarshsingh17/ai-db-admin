package ai.utkarsh.db_admin_assisstant.domain.recommendation.model;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceRecommendationTest {

    private PerformanceRecommendation newDraft() {
        return PerformanceRecommendation.draft(DatabaseId.generate(), null, RecommendationType.INDEX,
                RiskLevel.MEDIUM, "Add index", "explanation",
                new Sql("CREATE INDEX CONCURRENTLY idx_orders_customer_id ON orders(customer_id)"), "orders");
    }

    @Test
    void submitForApproval_movesDraftToPendingApproval() {
        PerformanceRecommendation recommendation = newDraft();

        recommendation.submitForApproval();

        assertThat(recommendation.getStatus()).isEqualTo(RecommendationStatus.PENDING_APPROVAL);
        assertThat(recommendation.pullDomainEvents()).hasSize(1);
    }

    @Test
    void approve_fromPendingApproval_succeedsAndRecordsDecision() {
        PerformanceRecommendation recommendation = newDraft();
        recommendation.submitForApproval();
        UUID adminId = UUID.randomUUID();

        recommendation.approve(adminId, "looks safe");

        assertThat(recommendation.getStatus()).isEqualTo(RecommendationStatus.APPROVED);
        assertThat(recommendation.getApprovalDecisions()).hasSize(1);
        assertThat(recommendation.getApprovalDecisions().getFirst().getAdminUserId()).isEqualTo(adminId);
    }

    @Test
    void approve_whenNotPendingApproval_throws() {
        PerformanceRecommendation recommendation = newDraft(); // still DRAFT

        assertThatThrownBy(() -> recommendation.approve(UUID.randomUUID(), null))
                .isInstanceOf(InvalidRecommendationStateException.class);
    }

    @Test
    void apply_lifecycle_appliedOnSuccess() {
        PerformanceRecommendation recommendation = newDraft();
        recommendation.submitForApproval();
        recommendation.approve(UUID.randomUUID(), null);

        recommendation.startApplying();
        recommendation.markApplied(UUID.randomUUID());

        assertThat(recommendation.getStatus()).isEqualTo(RecommendationStatus.APPLIED);
        assertThat(recommendation.getAppliedAt()).isNotNull();
    }

    @Test
    void apply_lifecycle_failedRecordsReason() {
        PerformanceRecommendation recommendation = newDraft();
        recommendation.submitForApproval();
        recommendation.approve(UUID.randomUUID(), null);
        recommendation.startApplying();

        recommendation.markFailed(UUID.randomUUID(), "connection refused");

        assertThat(recommendation.getStatus()).isEqualTo(RecommendationStatus.FAILED);
        assertThat(recommendation.getFailureReason()).isEqualTo("connection refused");
    }

    @Test
    void reject_whenApproved_throws() {
        PerformanceRecommendation recommendation = newDraft();
        recommendation.submitForApproval();
        recommendation.approve(UUID.randomUUID(), null);

        assertThatThrownBy(() -> recommendation.reject(UUID.randomUUID(), "changed my mind"))
                .isInstanceOf(InvalidRecommendationStateException.class);
    }
}
