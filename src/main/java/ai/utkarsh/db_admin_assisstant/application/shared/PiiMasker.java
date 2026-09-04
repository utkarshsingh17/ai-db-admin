package ai.utkarsh.db_admin_assisstant.application.shared;

import java.util.Locale;

/**
 * Partial-redaction masking for a single value from a column flagged sensitive (see
 * {@code QueryResultMasker}, which decides *whether* a value gets masked — this class only decides
 * *how*).
 */
public final class PiiMasker {

    private PiiMasker() {
    }

    public static String mask(String columnName, String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String lowerColumn = columnName.toLowerCase(Locale.ROOT);
        if (lowerColumn.contains("email") || value.contains("@")) {
            return maskEmail(value);
        }
        if (lowerColumn.contains("phone")) {
            return maskPhone(value);
        }
        return maskGeneric(value);
    }

    private static String maskEmail(String value) {
        int at = value.indexOf('@');
        if (at <= 0) {
            return maskGeneric(value);
        }
        String local = value.substring(0, at);
        String domain = value.substring(at);
        String visible = local.substring(0, 1);
        return visible + "***" + domain;
    }

    private static String maskPhone(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() < 4) {
            return "***";
        }
        String lastFour = digits.substring(digits.length() - 4);
        return "***-***-" + lastFour;
    }

    private static String maskGeneric(String value) {
        if (value.length() <= 2) {
            return "***";
        }
        return value.charAt(0) + "***" + value.charAt(value.length() - 1);
    }
}
