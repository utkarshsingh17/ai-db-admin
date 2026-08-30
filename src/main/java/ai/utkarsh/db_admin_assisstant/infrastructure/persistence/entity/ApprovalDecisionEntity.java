package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.ApprovalDecisionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation_approval")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalDecisionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recommendation_approval_recommendation"))
    private PerformanceRecommendationEntity recommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalDecisionType decision;

    @Column(name = "admin_user_id", nullable = false)
    private UUID adminUserId;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;
}
