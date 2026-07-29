package com.railops.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.railops.user.domain.User;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
        "railops-test-secret-key-for-jwt-token-provider",
        3600
    );

    @Test
    void createsValidToken() {
        User user = User.createUser("user@example.com", "encoded-password", "홍길동");
        UserPrincipal principal = UserPrincipal.from(user);

        String token = jwtTokenProvider.createToken(principal);

        assertThat(jwtTokenProvider.isValid(token)).isTrue();
        assertThat(jwtTokenProvider.getSubject(token)).isEqualTo("user@example.com");
    }

    @Test
    void rejectsMalformedToken() {
        assertThat(jwtTokenProvider.isValid("invalid-token")).isFalse();
    }
}