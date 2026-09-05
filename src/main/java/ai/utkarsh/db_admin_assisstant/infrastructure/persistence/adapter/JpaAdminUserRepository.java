package ai.utkarsh.db_admin_assisstant.infrastructure.persistence.adapter;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUserId;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.out.AdminUserRepository;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AdminUserEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.mapper.AdminUserMapper;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.AdminUserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAdminUserRepository implements AdminUserRepository {

    private final AdminUserJpaRepository springDataRepository;

    public JpaAdminUserRepository(AdminUserJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public AdminUser save(AdminUser user) {
        AdminUserEntity entity = springDataRepository.findById(user.getId().value())
                .orElseGet(AdminUserEntity::new);
        AdminUserMapper.updateEntity(entity, user);
        springDataRepository.save(entity);
        return user;
    }

    @Override
    public Optional<AdminUser> findById(AdminUserId id) {
        return springDataRepository.findById(id.value()).map(AdminUserMapper::toDomain);
    }

    @Override
    public Optional<AdminUser> findByEmail(String email) {
        return springDataRepository.findByEmail(email).map(AdminUserMapper::toDomain);
    }

    @Override
    public List<AdminUser> findAll() {
        return springDataRepository.findAll().stream().map(AdminUserMapper::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }

    @Override
    public boolean existsAny() {
        return springDataRepository.count() > 0;
    }

    @Override
    public long countByRoleAndEnabledTrue(AdminRole role) {
        return springDataRepository.countByRoleAndEnabledTrue(role.name());
    }

    @Override
    public Optional<AdminUser> findEarliestByRole(AdminRole role) {
        return springDataRepository.findFirstByRoleOrderByCreatedAtAsc(role.name()).map(AdminUserMapper::toDomain);
    }
}
