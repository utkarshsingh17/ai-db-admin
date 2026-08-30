package ai.utkarsh.db_admin_assisstant.domain.monitoring.model;

import java.util.Objects;

/** Normalized-query hash identifying a query shape across executions (params stripped). */
public record QueryFingerprint(String value) {

    public QueryFingerprint {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("QueryFingerprint must not be blank");
        }
    }
}
