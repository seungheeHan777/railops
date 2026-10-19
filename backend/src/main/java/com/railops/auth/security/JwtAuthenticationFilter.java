package com.railops.auth.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokens;
    private final CustomUserDetailsService users;
    public JwtAuthenticationFilter(JwtTokenProvider tokens, CustomUserDetailsService users) { this.tokens = tokens; this.users = users; }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            if (!header.startsWith("Bearer ") || !tokens.isValid(header.substring(7))) { unauthorized(response); return; }
            try {
                String token = header.substring(7);
                UserPrincipal user = (UserPrincipal) users.loadUserByUsername(tokens.getSubject(token));
                if (!user.isEnabled() || user.getTokenVersion() != tokens.getTokenVersion(token)) { unauthorized(response); return; }
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            } catch (UsernameNotFoundException e) { unauthorized(response); return; }
        }
        chain.doFilter(request, response);
    }
    private void unauthorized(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"data\":null,\"code\":\"AUTH_REQUIRED\",\"message\":\"로그인이 필요합니다.\"}");
    }
}
