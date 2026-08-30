package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.adapter;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.out.PerformanceRecommendationRepository;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.PerformanceRecommendationEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper.PerformanceRecommendationMapper;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.PerformanceRecommendationJpaRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaPerformanceRecommendationRepository implements PerformanceRecommendationRepository {

    private final PerformanceRecommendationJpaRepository springDataRepository;

    public JpaPerformanceRecommendationRepository(PerformanceRecommendationJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public PerformanceRecommendation save(PerformanceRecommendation recommendation) {
        PerformanceRecommendationEntity entity = springDataRepository.findById(recommendation.getId().value())
                .orElseGet(PerformanceRecommendationEntity::new);
        PerformanceRecommendationMapper.updateEntity(entity, recommendation);
        springDataRepository.save(entity);
        // Return the caller's own instance rather than reconstituting a fresh one from the saved
        // entity: reconstitute() always starts with an empty domain-event list, so returning a new
        // object here would silently discard any events the caller collected before calling save().
        return recommendation;
    }

    @Override
    public Optional<PerformanceRecommendation> findById(RecommendationId id) {
        return springDataRepository.findById(id.value()).map(PerformanceRecommendationMapper::toDomain);
    }

    @Override
    public List<PerformanceRecommendation> findByStatus(RecommendationStatus status, int limit) {
        return springDataRepository.findByStatusOrderByCreatedAtDesc(status, Limit.of(limit)).stream()
                .map(PerformanceRecommendationMapper::toDomain).toList();
    }

    @Override
    public List<PerformanceRecommendation> findByDatabase(DatabaseId databaseId, int limit) {
        return springDataRepository.findByDatabaseIdOrderByCreatedAtDesc(databaseId.value(), Limit.of(limit))
                .stream().map(PerformanceRecommendationMapper::toDomain).toList();
    }
}
