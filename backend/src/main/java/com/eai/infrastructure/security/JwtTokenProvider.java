package com.eai.infrastructure.security;

import com.eai.application.common.UnauthorizedException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.security.TokenProvider;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider implements TokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String JWT_ALGORITHM = "HS256";
    private static final String JWT_TYPE = "JWT";
    private static final int MIN_SHARED_SECRET_BYTES = 32;
    private static final Set<String> LOCAL_PROFILES = Set.of("dev", "test", "demo");
    private static final Set<String> INSECURE_DEFAULT_SECRETS = Set.of(
            "eai-local-development-secret-change-me",
            "eai-test-secret-change-me"
    );

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long accessTokenTtlMinutes;

    public JwtTokenProvider(
            ObjectMapper objectMapper,
            @Value("${eai.security.jwt-secret:eai-local-development-secret-change-me}") String secret,
            @Value("${eai.security.access-token-ttl-minutes:15}") long accessTokenTtlMinutes,
            Environment environment
    ) {
        this.objectMapper = objectMapper;
        validateSecret(secret, environment);
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    @Override
    public String createAccessToken(User user) {
        try {
            String header = encodeJson(Map.of("alg", JWT_ALGORITHM, "typ", JWT_TYPE));
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", user.getId().toString());
            claims.put("email", user.getEmail());
            claims.put("companyId", user.getCompanyId() == null ? null : user.getCompanyId().toString());
            claims.put("storeId", user.getStoreId() == null ? null : user.getStoreId().toString());
            claims.put("roles", user.getRoles().stream().map(Enum::name).sorted().toList());
            claims.put("exp", Instant.now().plusSeconds(accessTokenTtlMinutes * 60).getEpochSecond());
            String payload = encodeJson(claims);
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + encode(sign(unsignedToken));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create access token", exception);
        }
    }

    @Override
    public AuthenticatedUser parseAccessToken(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                throw new UnauthorizedException("Invalid access token");
            }
            validateHeader(parts[0]);
            if (!signatureMatches(parts[0] + "." + parts[1], parts[2])) {
                throw new UnauthorizedException("Invalid access token");
            }
            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            Number expiresAt = (Number) payload.get("exp");
            if (expiresAt == null || expiresAt.longValue() <= Instant.now().getEpochSecond()) {
                throw new UnauthorizedException("Access token expired");
            }
            String subject = requiredString(payload, "sub");
            String email = requiredString(payload, "email");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) payload.get("roles");
            if (roles == null || roles.isEmpty() || roles.stream().anyMatch(role -> role == null || role.isBlank())) {
                throw new UnauthorizedException("Invalid access token");
            }
            Set<UserRole> parsedRoles = roles.stream()
                    .map(UserRole::valueOf)
                    .collect(Collectors.toSet());
            return new AuthenticatedUser(
                    UUID.fromString(subject),
                    email,
                    parseOptionalUuid(payload.get("companyId")),
                    parseOptionalUuid(payload.get("storeId")),
                    parsedRoles
            );
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UnauthorizedException("Invalid access token");
        }
    }

    private String encodeJson(Object value) throws Exception {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value);
    }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private void validateHeader(String encodedHeader) throws Exception {
        Map<String, Object> header = objectMapper.readValue(
                Base64.getUrlDecoder().decode(encodedHeader),
                new TypeReference<>() {
                }
        );
        if (!JWT_ALGORITHM.equals(header.get("alg")) || !JWT_TYPE.equals(header.get("typ"))) {
            throw new UnauthorizedException("Invalid access token");
        }
    }

    private boolean signatureMatches(String unsignedToken, String encodedSignature) throws Exception {
        byte[] receivedSignature = Base64.getUrlDecoder().decode(encodedSignature);
        return MessageDigest.isEqual(sign(unsignedToken), receivedSignature);
    }

    private String requiredString(Map<String, Object> payload, String claim) {
        Object value = payload.get(claim);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new UnauthorizedException("Invalid access token");
        }
        return stringValue;
    }

    private UUID parseOptionalUuid(Object value) {
        return value == null ? null : UUID.fromString((String) value);
    }

    private void validateSecret(String secret, Environment environment) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is required");
        }
        if (isLocalProfile(environment)) {
            return;
        }
        if (INSECURE_DEFAULT_SECRETS.contains(secret) || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SHARED_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must have at least 32 bytes outside local profiles");
        }
    }

    private boolean isLocalProfile(Environment environment) {
        String[] activeProfiles = environment == null ? new String[0] : environment.getActiveProfiles();
        String[] profiles = activeProfiles.length == 0 && environment != null ? environment.getDefaultProfiles() : activeProfiles;
        return Arrays.stream(profiles)
                .filter(Objects::nonNull)
                .anyMatch(LOCAL_PROFILES::contains);
    }
}
