package ai.utkarsh.db_admin_assisstant.infrastructure.security;

import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.entity.AdminUserEntity;
import ai.utkarsh.db_admin_assisstant.infrastructure.persistence.repository.AdminUserJpaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserJpaRepository repository;

    public AdminUserDetailsService(AdminUserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminUserEntity user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No admin user with email " + email));
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                .build();
    }
}
