package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.RegisterAdminUserUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.AuthService;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.AuthResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.LoginRequest;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegisterAdminUserUseCase registerUseCase;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ApiResponse.ok(AuthResponse.of(token));
    }

    /**
     * Public — see {@link RegisterAdminUserUseCase} for the role-assignment rule. Logs the new
     * account straight in (same token shape as {@link #login}) so registering doesn't require a
     * separate login step.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.register(request.email(), request.password());
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(AuthResponse.of(token)));
    }
}
