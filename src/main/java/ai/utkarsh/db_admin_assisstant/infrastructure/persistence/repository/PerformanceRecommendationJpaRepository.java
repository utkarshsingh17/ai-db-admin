package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.PerformanceRecommendationEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerformanceRecommendationJpaRepository
        extends JpaRepository<PerformanceRecommendationEntity, UUID> {

    @EntityGraph(attributePaths = "approvalDecisions")
    Optional<PerformanceRecommendationEntity> findById(UUID id);

    @EntityGraph(attributePaths = "approvalDecisions")
    List<PerformanceRecommendationEntity> findByStatusOrderByCreatedAtDesc(RecommendationStatus status, Limit limit);

    @EntityGraph(attributePaths = "approvalDecisions")
    List<PerformanceRecommendationEntity> findByDatabaseIdOrderByCreatedAtDesc(UUID databaseId, Limit limit);
}
