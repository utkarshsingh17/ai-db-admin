package ai.utkarsh.db_admin_assisstant.domain.masking.model;

import ai.utkarsh.db_admin_assisstant.domain.shared.DomainException;

public class SensitiveColumnNotFoundException extends DomainException {

    public SensitiveColumnNotFoundException(SensitiveColumnId id) {
        super("SENSITIVE_COLUMN_NOT_FOUND", "No sensitive column marking found with id: " + id.value());
    }
}
