package com.eai.infrastructure.security;

import com.eai.application.common.UnauthorizedException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.security.TokenProvider;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider implements TokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long accessTokenTtlMinutes;

    public JwtTokenProvider(
            ObjectMapper objectMapper,
            @Value("${eai.security.jwt-secret:eai-local-development-secret-change-me}") String secret,
            @Value("${eai.security.access-token-ttl-minutes:15}") long accessTokenTtlMinutes
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    @Override
    public String createAccessToken(User user) {
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(Map.of(
                    "sub", user.getId().toString(),
                    "email", user.getEmail(),
                    "companyId", user.getCompanyId().toString(),
                    "storeId", user.getStoreId().toString(),
                    "roles", user.getRoles().stream().map(Enum::name).sorted().toList(),
                    "exp", Instant.now().plusSeconds(accessTokenTtlMinutes * 60).getEpochSecond()
            ));
            String unsignedToken = header + "." + payload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create access token", exception);
        }
    }

    @Override
    public AuthenticatedUser parseAccessToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !sign(parts[0] + "." + parts[1]).equals(parts[2])) {
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
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) payload.get("roles");
            Set<UserRole> parsedRoles = roles.stream()
                    .map(UserRole::valueOf)
                    .collect(Collectors.toSet());
            return new AuthenticatedUser(
                    UUID.fromString((String) payload.get("sub")),
                    (String) payload.get("email"),
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

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private UUID parseOptionalUuid(Object value) {
        return value == null ? null : UUID.fromString((String) value);
    }
}
