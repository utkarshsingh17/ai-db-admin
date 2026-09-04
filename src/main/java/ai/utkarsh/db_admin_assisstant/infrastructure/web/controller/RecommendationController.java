package ai.utkarsh.db_admin_assisstant.infrastructure.web.controller;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationId;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationStatus;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RecommendationType;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.RiskLevel;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApplyAiQueryUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApplyRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ApproveRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.ListRecommendationsUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.RejectRecommendationUseCase;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.SubmitManualSqlUseCase;
import ai.utkarsh.db_admin_assisstant.infrastructure.security.CurrentAdminResolver;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApproveRecommendationRequest;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.RecommendationApplyResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.RecommendationResponse;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.RejectRecommendationRequest;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.SubmitManualSqlRequest;
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
    private final ApplyAiQueryUseCase applyAiQueryUseCase;
    private final SubmitManualSqlUseCase submitManualSqlUseCase;
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

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<RecommendationResponse>> submitManual(
            @Valid @RequestBody SubmitManualSqlRequest request, Authentication authentication) {
        PerformanceRecommendation recommendation = submitManualSqlUseCase.submitManualSql(
                new SubmitManualSqlUseCase.SubmitManualSqlCommand(DatabaseId.of(request.databaseId()),
                        request.title(), request.explanation(), request.proposedSql(),
                        RiskLevel.valueOf(request.riskLevel()), request.targetObject(),
                        currentAdminResolver.resolveId(authentication)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(RecommendationResponse.from(recommendation)));
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
    public ApiResponse<RecommendationApplyResponse> apply(@PathVariable String id, Authentication authentication) {
        RecommendationId recommendationId = RecommendationId.of(id);
        PerformanceRecommendation recommendation = listRecommendationsUseCase.getById(recommendationId);

        if (recommendation.getType() == RecommendationType.AI_QUERY) {
            ApplyAiQueryUseCase.AiQueryApplyResult result = applyAiQueryUseCase.apply(recommendationId,
                    currentAdminResolver.resolveId(authentication), currentAdminResolver.isAdmin(authentication));
            return ApiResponse.ok(RecommendationApplyResponse.ofQuery(result.recommendation(), result.result(),
                    result.optimizationRecommendationId()));
        }

        PerformanceRecommendation applied = applyRecommendationUseCase.apply(recommendationId,
                currentAdminResolver.resolveId(authentication));
        return ApiResponse.ok(RecommendationApplyResponse.of(applied));
    }
}
