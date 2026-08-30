package ai.utkarsh.db_admin_assisstant.application.audit;

import java.util.LinkedHashMap;
import java.util.Map;

/** Minimal, dependency-free JSON object builder for compact audit-log payload strings. */
public final class JsonPayload {

    private final Map<String, String> fields = new LinkedHashMap<>();

    public static JsonPayload of() {
        return new JsonPayload();
    }

    public JsonPayload put(String key, Object value) {
        fields.put(key, value == null ? null : String.valueOf(value));
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            sb.append('"').append(escape(entry.getKey())).append("\":");
            if (entry.getValue() == null) {
                sb.append("null");
            } else {
                sb.append('"').append(escape(entry.getValue())).append('"');
            }
            first = false;
        }
        return sb.append('}').toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
