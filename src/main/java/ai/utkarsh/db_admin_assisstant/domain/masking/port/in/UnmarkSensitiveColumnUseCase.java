package ai.utkarsh.db_admin_assisstant.domain.masking.port.in;

import ai.utkarsh.db_admin_assisstant.domain.masking.model.SensitiveColumnId;

import java.util.UUID;

public interface UnmarkSensitiveColumnUseCase {

    void unmark(SensitiveColumnId id, UUID actingAdminId);
}
