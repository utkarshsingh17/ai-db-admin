package ai.utkarsh.db_admin_assisstant.infrastructure.web.exception;

import ai.utkarsh.db_admin_assisstant.domain.shared.DomainException;
import ai.utkarsh.db_admin_assisstant.infrastructure.web.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        return ResponseEntity.status(statusFor(ex)).body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("INVALID_CREDENTIALS", "Invalid email or password"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_FAILED", "Invalid input", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private HttpStatus statusFor(DomainException ex) {
        return switch (ex.getErrorCode()) {
            case "RECOMMENDATION_NOT_FOUND", "MONITORED_DATABASE_NOT_FOUND", "ADMIN_USER_NOT_FOUND",
                    "SENSITIVE_COLUMN_NOT_FOUND" ->
                HttpStatus.NOT_FOUND;
            case "INVALID_RECOMMENDATION_STATE" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
