package com.railops.auth.service;

import com.railops.auth.dto.LoginRequest;
import com.railops.auth.dto.LoginResponse;
import com.railops.auth.dto.SignupRequest;
import com.railops.auth.dto.UserSummaryResponse;
import com.railops.auth.security.JwtTokenProvider;
import com.railops.auth.security.UserPrincipal;
import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.user.domain.User;
import com.railops.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserSummaryResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.createUser(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.name()
        );

        return UserSummaryResponse.from(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = jwtTokenProvider.createToken(principal);
        return LoginResponse.bearer(accessToken, UserSummaryResponse.from(user));
    }

    public UserSummaryResponse me(UserPrincipal principal) {
        User user = userRepository.findByEmail(principal.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserSummaryResponse.from(user);
    }
}