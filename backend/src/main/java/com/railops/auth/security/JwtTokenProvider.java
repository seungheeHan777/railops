package com.railops.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final long expirationSeconds;

    public JwtTokenProvider(
        @Value("${railops.jwt.secret:railops-local-development-secret-key-change-me}") String secret,
        @Value("${railops.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(UserPrincipal principal) {
        long expiresAt = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + escape(principal.getEmail()) + "\",\"uid\":" + principal.getId() + ",\"exp\":" + expiresAt + "}";
        String unsignedToken = encode(header) + "." + encode(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public boolean isValid(String token) {
        try {
            String[] parts = split(token);
            String unsignedToken = parts[0] + "." + parts[1];
            if (!sign(unsignedToken).equals(parts[2])) {
                return false;
            }
            return extractExpiration(parts[1]) > Instant.now().getEpochSecond();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public String getSubject(String token) {
        String[] parts = split(token);
        String payload = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        String marker = "\"sub\":\"";
        int start = payload.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Token subject is missing.");
        }
        int valueStart = start + marker.length();
        int valueEnd = payload.indexOf('"', valueStart);
        if (valueEnd < 0) {
            throw new IllegalArgumentException("Token subject is invalid.");
        }
        return payload.substring(valueStart, valueEnd).replace("\\\"", "\"");
    }

    private String encode(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT signing failed.", exception);
        }
    }

    private String[] split(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format.");
        }
        return parts;
    }

    private long extractExpiration(String encodedPayload) {
        String payload = new String(BASE64_URL_DECODER.decode(encodedPayload), StandardCharsets.UTF_8);
        String marker = "\"exp\":";
        int start = payload.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Token expiration is missing.");
        }
        int valueStart = start + marker.length();
        int valueEnd = payload.indexOf('}', valueStart);
        return Long.parseLong(payload.substring(valueStart, valueEnd));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}