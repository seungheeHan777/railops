package com.railops.user.service;

import com.railops.auth.dto.UserSummaryResponse;
import com.railops.auth.security.UserPrincipal;
import com.railops.common.error.*;
import com.railops.operation.*;
import com.railops.reservation.repository.ReservationRepository;
import com.railops.user.domain.*;
import com.railops.user.dto.*;
import com.railops.user.repository.UserRepository;
import java.time.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class UserService {
    private final UserRepository users;
    private final ReservationRepository reservations;
    private final PasswordEncoder encoder;
    private final OperationLogService logs;
    private final Clock clock;
    public UserService(UserRepository users, ReservationRepository reservations, PasswordEncoder encoder, OperationLogService logs, Clock clock) {
        this.users = users; this.reservations = reservations; this.encoder = encoder; this.logs = logs; this.clock = clock;
    }
    public UserSummaryResponse update(UserPrincipal principal, ProfileUpdateRequest request) {
        User user = current(principal); user.rename(request.name());
        logs.record(OperationAction.PROFILE_UPDATED, "USER", user.getId());
        return UserSummaryResponse.from(user);
    }
    public void password(UserPrincipal principal, PasswordChangeRequest request) {
        User user = current(principal); verifyPassword(user, request.currentPassword());
        if (request.newPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new BusinessException(ErrorCode.INVALID_REQUEST);
        user.changePassword(encoder.encode(request.newPassword()));
        logs.record(OperationAction.PASSWORD_CHANGED, "USER", user.getId());
    }
    public void withdraw(UserPrincipal principal, WithdrawalRequest request) {
        User user = current(principal); verifyPassword(user, request.currentPassword());
        if (user.getRole() == UserRole.ADMIN) throw new BusinessException(ErrorCode.ACCESS_DENIED);
        if (reservations.hasActiveReservations(user.getId(), LocalDateTime.now(clock))) throw new BusinessException(ErrorCode.ACTIVE_RESERVATIONS_EXIST);
        user.withdraw(); logs.record(OperationAction.ACCOUNT_WITHDRAWN, "USER", user.getId());
    }
    public void logout(UserPrincipal principal) {
        User user = current(principal); user.revokeTokens(); logs.record(OperationAction.LOGOUT, "USER", user.getId());
    }
    private User current(UserPrincipal p) {
        User user = users.findByIdForUpdate(p.getId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.isActive() || user.getTokenVersion() != p.getTokenVersion()) throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        return user;
    }
    private void verifyPassword(User user, String password) {
        if (!encoder.matches(password, user.getPassword())) throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
}
