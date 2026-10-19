package com.railops.auth.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final byte[] secret;
    private final long expirationSeconds;
    public JwtTokenProvider(@Value("${railops.jwt.secret}") String secret,
        @Value("${railops.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) throw new IllegalArgumentException("JWT secret must contain at least 32 UTF-8 bytes");
        this.expirationSeconds = expirationSeconds;
    }
    public String createToken(UserPrincipal principal) {
        try {
            var claims = new JWTClaimsSet.Builder().subject(principal.getEmail())
                .claim("uid", principal.getId()).claim("ver", principal.getTokenVersion())
                .issueTime(Date.from(Instant.now())).jwtID(java.util.UUID.randomUUID().toString())
                .expirationTime(Date.from(Instant.now().plusSeconds(expirationSeconds))).build();
            var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims);
            token.sign(new MACSigner(secret));
            return token.serialize();
        } catch (JOSEException e) { throw new IllegalStateException("JWT signing failed", e); }
    }
    public boolean isValid(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            var claims = jwt.getJWTClaimsSet();
            return JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) && jwt.verify(new MACVerifier(secret))
                && claims.getExpirationTime() != null && claims.getExpirationTime().toInstant().isAfter(Instant.now())
                && claims.getSubject() != null && claims.getLongClaim("ver") != null;
        } catch (Exception e) { return false; }
    }
    public String getSubject(String token) { return claims(token).getSubject(); }
    public long getTokenVersion(String token) {
        try { return claims(token).getLongClaim("ver"); }
        catch (java.text.ParseException e) { throw new IllegalArgumentException("Invalid token version", e); }
    }
    private JWTClaimsSet claims(String token) {
        try { return SignedJWT.parse(token).getJWTClaimsSet(); }
        catch (java.text.ParseException e) { throw new IllegalArgumentException("Invalid token", e); }
    }
}
