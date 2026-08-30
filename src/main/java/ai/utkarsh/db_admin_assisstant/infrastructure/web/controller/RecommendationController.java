package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApplyRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApproveRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ListRecommendationsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.RejectRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApproveRecommendationRequest;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.RecommendationResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.RejectRecommendationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final ListRecommendationsUseCase listRecommendationsUseCase;
    private final ApproveRecommendationUseCase approveRecommendationUseCase;
    private final RejectRecommendationUseCase rejectRecommendationUseCase;
    private final ApplyRecommendationUseCase applyRecommendationUseCase;
    private final CurrentAdminResolver currentAdminResolver;

    @GetMapping
    public ApiResponse<List<RecommendationResponse>> list(
            @RequestParam(defaultValue = "PENDING_APPROVAL") RecommendationStatus status,
            @RequestParam(defaultValue = "50") int limit) {
        List<RecommendationResponse> response = listRecommendationsUseCase.listByStatus(status, Math.min(limit, 200))
                .stream().map(RecommendationResponse::from).toList();
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<RecommendationResponse> getById(@PathVariable String id) {
        return ApiResponse
                .ok(RecommendationResponse.from(listRecommendationsUseCase.getById(RecommendationId.of(id))));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<RecommendationResponse> approve(@PathVariable String id,
            @RequestBody(required = false) ApproveRecommendationRequest request, Authentication authentication) {
        String comment = request == null ? null : request.comment();
        PerformanceRecommendation recommendation = approveRecommendationUseCase.approve(RecommendationId.of(id),
                currentAdminResolver.resolveId(authentication), comment);
        return ApiResponse.ok(RecommendationResponse.from(recommendation));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<RecommendationResponse> reject(@PathVariable String id,
            @RequestBody(required = false) RejectRecommendationRequest request, Authentication authentication) {
        String reason = request == null ? null : request.reason();
        PerformanceRecommendation recommendation = rejectRecommendationUseCase.reject(RecommendationId.of(id),
                currentAdminResolver.resolveId(authentication), reason);
        return ApiResponse.ok(RecommendationResponse.from(recommendation));
    }

    @PostMapping("/{id}/apply")
    public ApiResponse<RecommendationResponse> apply(@PathVariable String id, Authentication authentication) {
        PerformanceRecommendation recommendation = applyRecommendationUseCase.apply(RecommendationId.of(id),
                currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(RecommendationResponse.from(recommendation));
    }
}
