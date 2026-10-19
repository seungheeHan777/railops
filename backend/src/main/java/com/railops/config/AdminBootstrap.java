package com.railops.config;

import com.railops.operation.*;
import com.railops.user.domain.*;
import com.railops.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "railops.bootstrap-admin.enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final OperationLogService logs;
    private final String email;
    private final String password;
    public AdminBootstrap(UserRepository users, PasswordEncoder encoder, OperationLogService logs,
        @Value("${RAILOPS_ADMIN_EMAIL:}") String email, @Value("${RAILOPS_ADMIN_PASSWORD:}") String password) {
        this.users = users; this.encoder = encoder; this.logs = logs; this.email = email; this.password = password;
    }
    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password.length() < 12
            || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Bootstrap requires valid RAILOPS_ADMIN_EMAIL and a 12+ character password (max 72 UTF-8 bytes)");
        }
        var existing = users.findByEmail(email);
        if (existing.isPresent()) {
            if (existing.get().getRole() != UserRole.ADMIN || !existing.get().isActive()) throw new IllegalStateException("Bootstrap refuses to promote or reactivate an existing account");
            return;
        }
        var admin = users.save(User.createAdmin(email, encoder.encode(password), "관리자"));
        logs.record(OperationAction.ADMIN_CREATED, "USER", admin.getId());
    }
}
