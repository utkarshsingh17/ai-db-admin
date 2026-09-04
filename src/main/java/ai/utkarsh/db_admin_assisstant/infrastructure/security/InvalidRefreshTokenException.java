package ai.utkarsh.db_admin_assisstant.infrastructure.security;

/** Distinct from {@link org.springframework.security.authentication.BadCredentialsException} so the
 * client can tell "your session ended, log in again" apart from "wrong email/password" — both are
 * 401s, but the frontend reacts to them differently (full logout vs. an inline form error). */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired");
    }
}
