package com.eai.application.lead;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNormalizerTest {

    @DisplayName("Normalizador retorna nulo para telefone ausente")
    @Test
    void normalizeReturnsNullForMissingPhone() {
        assertThat(PhoneNormalizer.normalize(null)).isNull();
        assertThat(PhoneNormalizer.normalize("   ")).isNull();
    }

    @DisplayName("Normalizador converte telefone brasileiro para E164")
    @Test
    void normalizeBrazilianPhoneToE164() {
        assertThat(PhoneNormalizer.normalize("(11) 99999-8888")).isEqualTo("+5511999998888");
        assertThat(PhoneNormalizer.normalize("1133334444")).isEqualTo("+551133334444");
    }

    @DisplayName("Normalizador preserva telefone E164 valido")
    @Test
    void normalizeKeepsValidE164Phone() {
        assertThat(PhoneNormalizer.normalize("+5511999998888")).isEqualTo("+5511999998888");
    }

    @DisplayName("Normalizador adiciona mais quando numero com DDI 55 vier sem sinal")
    @Test
    void normalizeAddsPlusToPhoneWithBrazilianCountryCode() {
        assertThat(PhoneNormalizer.normalize("5511999998888")).isEqualTo("+5511999998888");
    }

    @DisplayName("Normalizador rejeita telefone invalido")
    @Test
    void normalizeRejectsInvalidPhone() {
        assertThatThrownBy(() -> PhoneNormalizer.normalize("+001234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerPhone must be E.164 or a Brazilian phone with 10 or 11 digits");
        assertThatThrownBy(() -> PhoneNormalizer.normalize("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerPhone must be E.164 or a Brazilian phone with 10 or 11 digits");
    }
}
