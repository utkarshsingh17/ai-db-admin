package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

public record AuthResponse(String accessToken, String tokenType) {

    public static AuthResponse of(String accessToken) {
        return new AuthResponse(accessToken, "Bearer");
    }
}
