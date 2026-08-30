package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.MetricSnapshotEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MetricSnapshotJpaRepository extends JpaRepository<MetricSnapshotEntity, UUID> {

    List<MetricSnapshotEntity> findByDatabaseIdOrderByCapturedAtDesc(UUID databaseId, Limit limit);

    @Modifying
    @Query("delete from MetricSnapshotEntity m where m.capturedAt < :cutoff")
    int deleteByCapturedAtBefore(@Param("cutoff") Instant cutoff);
}
