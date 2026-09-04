package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import ai.utkarsh.db_admin_assisstant.domain.query.model.AiQuerySubmission;

public record AiQuerySubmissionResponse(String sql, String explanation, String recommendationId) {

    public static AiQuerySubmissionResponse from(AiQuerySubmission submission) {
        return new AiQuerySubmissionResponse(submission.sql(), submission.explanation(),
                submission.recommendationId() == null ? null : submission.recommendationId().value().toString());
    }
}
