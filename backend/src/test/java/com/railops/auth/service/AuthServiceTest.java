package com.railops.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.railops.auth.dto.LoginRequest;
import com.railops.auth.dto.LoginResponse;
import com.railops.auth.dto.SignupRequest;
import com.railops.auth.dto.UserSummaryResponse;
import com.railops.auth.security.JwtTokenProvider;
import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.user.domain.User;
import com.railops.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtTokenProvider = new JwtTokenProvider("railops-test-secret-key-for-auth-service", 3600);
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void signupCreatesUser() {
        SignupRequest request = new SignupRequest("user@example.com", "password1234", "홍길동");
        User savedUser = User.createUser(request.email(), "encoded-password", request.name());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserSummaryResponse response = authService.signup(request);

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    void signupRejectsDuplicatedEmail() {
        SignupRequest request = new SignupRequest("user@example.com", "password1234", "홍길동");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    void loginReturnsAccessToken() {
        String rawPassword = "password1234";
        User user = User.createUser("user@example.com", passwordEncoder.encode(rawPassword), "홍길동");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest(user.getEmail(), rawPassword));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(jwtTokenProvider.isValid(response.accessToken())).isTrue();
        assertThat(response.user().email()).isEqualTo(user.getEmail());
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = User.createUser("user@example.com", passwordEncoder.encode("password1234"), "홍길동");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest(user.getEmail(), "wrong-password")))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }
}