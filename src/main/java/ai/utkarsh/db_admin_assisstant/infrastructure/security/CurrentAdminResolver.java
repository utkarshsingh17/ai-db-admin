package ai.utkarsh.db_admin_assisstant.infrastructure.security;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AdminUserEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.AdminUserJpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the JWT-authenticated principal's email back to the admin_user row's id. */
@Component
public class CurrentAdminResolver {

    private final AdminUserJpaRepository repository;

    public CurrentAdminResolver(AdminUserJpaRepository repository) {
        this.repository = repository;
    }

    public UUID resolveId(Authentication authentication) {
        AdminUserEntity user = repository.findByEmail(authentication.getName())
                .orElseThrow(
                        () -> new IllegalStateException("Authenticated user not found: " + authentication.getName()));
        return user.getId();
    }

    /** Whether the JWT's authorities include {@code ROLE_DB_ADMIN} — no DB lookup needed, the role
     * is already embedded in the token. */
    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DB_ADMIN"));
    }
}
