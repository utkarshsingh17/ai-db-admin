package ai.utkarsh.db_admin_assisstant.application.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskerTest {

    @Test
    void masksEmailKeepingFirstCharacterAndDomain() {
        assertThat(PiiMasker.mask("email", "jane.doe@example.com")).isEqualTo("j***@example.com");
    }

    @Test
    void masksValueLookingLikeAnEmailEvenIfColumnNameDoesNot() {
        assertThat(PiiMasker.mask("contact", "jane.doe@example.com")).isEqualTo("j***@example.com");
    }

    @Test
    void masksPhoneKeepingLastFourDigits() {
        assertThat(PiiMasker.mask("phone_number", "+91-9876543210")).isEqualTo("***-***-3210");
    }

    @Test
    void masksGenericValueKeepingFirstAndLastCharacter() {
        assertThat(PiiMasker.mask("license_number", "DL12345678")).isEqualTo("D***8");
    }

    @Test
    void masksShortGenericValuesCompletely() {
        assertThat(PiiMasker.mask("code", "AB")).isEqualTo("***");
    }

    @Test
    void leavesNullAndBlankValuesUntouched() {
        assertThat(PiiMasker.mask("email", null)).isNull();
        assertThat(PiiMasker.mask("email", "")).isEmpty();
    }
}
