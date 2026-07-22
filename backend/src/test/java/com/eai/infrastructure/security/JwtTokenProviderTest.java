package com.eai.infrastructure.security;

import com.eai.application.common.UnauthorizedException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import com.eai.domain.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String STRONG_SECRET = "0123456789abcdef0123456789abcdef";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("Cria e interpreta token com claims obrigatorias")
    @Test
    void createsAndParsesTokenWithRequiredClaims() {
        JwtTokenProvider provider = provider(STRONG_SECRET, "prod");

        AuthenticatedUser authenticatedUser = provider.parseAccessToken(provider.createAccessToken(user()));

        assertThat(authenticatedUser.id()).isEqualTo(USER_ID);
        assertThat(authenticatedUser.email()).isEqualTo("admin@eai.com");
        assertThat(authenticatedUser.companyId()).isEqualTo(COMPANY_ID);
        assertThat(authenticatedUser.storeId()).isEqualTo(STORE_ID);
        assertThat(authenticatedUser.roles()).containsExactly(UserRole.ADMIN);
    }

    @DisplayName("Rejeita token com assinatura adulterada")
    @Test
    void rejectsTokenWithTamperedSignature() throws Exception {
        JwtTokenProvider provider = provider(STRONG_SECRET, "prod");
        String token = provider.createAccessToken(user());
        String[] parts = token.split("\\.", -1);
        String tamperedToken = parts[0] + "." + encode(validPayload()) + "." + parts[2];

        assertThatThrownBy(() -> provider.parseAccessToken(tamperedToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid access token");
    }

    @DisplayName("Rejeita token com algoritmo inesperado")
    @Test
    void rejectsTokenWithUnexpectedAlgorithm() throws Exception {
        JwtTokenProvider provider = provider(STRONG_SECRET, "prod");
        String token = signedToken(
                Map.of("alg", "none", "typ", "JWT"),
                validPayload(),
                STRONG_SECRET
        );

        assertThatThrownBy(() -> provider.parseAccessToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid access token");
    }

    @DisplayName("Rejeita token sem claims obrigatorias")
    @Test
    void rejectsTokenWithoutRequiredClaims() throws Exception {
        JwtTokenProvider provider = provider(STRONG_SECRET, "prod");
        String token = signedToken(
                Map.of("alg", "HS256", "typ", "JWT"),
                Map.of(
                        "email", "admin@eai.com",
                        "roles", Set.of("ADMIN"),
                        "exp", Instant.now().plusSeconds(60).getEpochSecond()
                ),
                STRONG_SECRET
        );

        assertThatThrownBy(() -> provider.parseAccessToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid access token");
    }

    @DisplayName("Rejeita segredo fraco fora de perfil local")
    @Test
    void rejectsWeakSecretOutsideLocalProfile() {
        assertThatThrownBy(() -> provider("short-secret", "prod"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret must have at least 32 bytes");
    }

    @DisplayName("Aceita segredo local fraco em perfil de teste")
    @Test
    void acceptsWeakLocalSecretInTestProfile() {
        JwtTokenProvider provider = provider("eai-test-secret-change-me", "test");

        assertThat(provider.createAccessToken(user())).isNotBlank();
    }

    private JwtTokenProvider provider(String secret, String profile) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        return new JwtTokenProvider(objectMapper, secret, 15, environment);
    }

    private Map<String, Object> validPayload() {
        return Map.of(
                "sub", USER_ID.toString(),
                "email", "admin@eai.com",
                "companyId", COMPANY_ID.toString(),
                "storeId", STORE_ID.toString(),
                "roles", Set.of("ADMIN"),
                "exp", Instant.now().plusSeconds(60).getEpochSecond()
        );
    }

    private String signedToken(Map<String, Object> header, Map<String, Object> payload, String secret) throws Exception {
        String unsignedToken = encode(header) + "." + encode(payload);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return unsignedToken + "." + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
    }

    private String encode(Object value) throws Exception {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private User user() {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new User(
                USER_ID,
                "Admin EAI",
                "admin@eai.com",
                "password-hash",
                null,
                "Administrador",
                COMPANY_ID,
                STORE_ID,
                UserStatus.ACTIVE,
                Set.of(UserRole.ADMIN),
                now,
                now
        );
    }
}
