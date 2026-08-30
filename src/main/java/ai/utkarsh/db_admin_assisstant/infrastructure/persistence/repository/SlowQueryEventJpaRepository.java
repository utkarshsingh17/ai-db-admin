package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.SlowQueryEventEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SlowQueryEventJpaRepository extends JpaRepository<SlowQueryEventEntity, UUID> {

    List<SlowQueryEventEntity> findByDatabaseIdOrderByCapturedAtDesc(UUID databaseId, Limit limit);

    @Modifying
    @Query("delete from SlowQueryEventEntity s where s.capturedAt < :cutoff")
    int deleteByCapturedAtBefore(@Param("cutoff") Instant cutoff);
}
