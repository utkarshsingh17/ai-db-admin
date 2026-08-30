package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.infrastructure.security.AuthService;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.AuthResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ApiResponse.ok(AuthResponse.of(token));
    }
}
