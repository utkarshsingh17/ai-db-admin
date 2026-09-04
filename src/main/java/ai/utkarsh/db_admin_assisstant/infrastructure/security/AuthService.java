package ai.utkarsh.db_admin_assisstant.infrastructure.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public AuthTokens login(String email, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return new AuthTokens(jwtService.generateAccessToken(user), jwtService.generateRefreshToken(user));
    }

    /**
     * Mints a fresh access token from a still-valid refresh token — the refresh token itself is
     * not rotated, so the same one keeps working until its own 7-day expiry (a fixed session
     * ceiling, not a sliding one). Re-loads the user rather than trusting any claim on the refresh
     * token itself, so a role change or a disabled account takes effect immediately instead of
     * waiting for the refresh token to expire.
     */
    public String refresh(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }
        String email = jwtService.extractUsername(refreshToken);
        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw new InvalidRefreshTokenException();
        }
        if (!user.isEnabled()) {
            throw new InvalidRefreshTokenException();
        }
        return jwtService.generateAccessToken(user);
    }

    public record AuthTokens(String accessToken, String refreshToken) {
    }
}
