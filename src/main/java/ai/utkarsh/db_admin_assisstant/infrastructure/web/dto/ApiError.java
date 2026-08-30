package ai.utkarsh.db_admin_assisstant.infrastructure.web.dto;

import java.util.List;

public record ApiError(String code, String message, List<String> details) {
}
