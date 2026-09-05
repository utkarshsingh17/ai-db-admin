package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminRole;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUser;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.model.AdminUserId;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.ChangeAdminUserRoleUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.CreateAdminUserUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.ListAdminUsersUseCase;
import ai.utkarsh.db_admin_assisstant.domain.adminuser.port.in.SetAdminUserEnabledUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.AdminUserResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ChangeAdminUserRoleRequest;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.CreateAdminUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin-users")
@RequiredArgsConstructor
public class AdminUserController {

    private final CreateAdminUserUseCase createUseCase;
    private final ListAdminUsersUseCase listUseCase;
    private final ChangeAdminUserRoleUseCase changeRoleUseCase;
    private final SetAdminUserEnabledUseCase setEnabledUseCase;
    private final CurrentAdminResolver currentAdminResolver;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminUserResponse>> create(@Valid @RequestBody CreateAdminUserRequest request,
            Authentication authentication) {
        AdminUser user = createUseCase.create(new CreateAdminUserUseCase.CreateAdminUserCommand(request.email(),
                request.password(), AdminRole.valueOf(request.role()),
                currentAdminResolver.resolveId(authentication)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(AdminUserResponse.from(user)));
    }

    @GetMapping
    public ApiResponse<List<AdminUserResponse>> list() {
        return ApiResponse.ok(listUseCase.listAll().stream().map(AdminUserResponse::from).toList());
    }

    @PostMapping("/{id}/role")
    public ApiResponse<AdminUserResponse> changeRole(@PathVariable String id,
            @Valid @RequestBody ChangeAdminUserRoleRequest request, Authentication authentication) {
        AdminUser user = changeRoleUseCase.changeRole(AdminUserId.of(id), AdminRole.valueOf(request.role()),
                currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(AdminUserResponse.from(user));
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<AdminUserResponse> disable(@PathVariable String id, Authentication authentication) {
        AdminUser user = setEnabledUseCase.setEnabled(AdminUserId.of(id), false,
                currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(AdminUserResponse.from(user));
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<AdminUserResponse> enable(@PathVariable String id, Authentication authentication) {
        AdminUser user = setEnabledUseCase.setEnabled(AdminUserId.of(id), true,
                currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(AdminUserResponse.from(user));
    }
}
