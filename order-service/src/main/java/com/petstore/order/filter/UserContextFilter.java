package com.petstore.order.filter;

import com.petstore.order.context.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class UserContextFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public UserContextFilter(@Value("${jwt.secret:dGhpcy1pcy1hLXNlY3VyZS1qd3Qtc2VjcmV0LWZvci1wZXQtc3RvcmUtdjEtZGV2ZWxvcG1lbnQtdGVzdC1rZXk=}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String gatewayUserId = request.getHeader("X-User-Id");
            String gatewayUserRole = request.getHeader("X-User-Role");
            String gatewayUserEmail = request.getHeader("X-User-Email");

            if (StringUtils.hasText(gatewayUserId)) {
                UserContext.setUserId(Long.parseLong(gatewayUserId));
                UserContext.setUserRole(gatewayUserRole != null ? gatewayUserRole : "CUSTOMER");
                UserContext.setUserEmail(gatewayUserEmail != null ? gatewayUserEmail : "");
            } else {
                // Fallback to Bearer token if called directly
                String bearerToken = request.getHeader("Authorization");
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);
                    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                    Object userIdVal = claims.get("userId");
                    if (userIdVal != null) {
                        Long userId = userIdVal instanceof Number ? ((Number) userIdVal).longValue() : Long.parseLong(userIdVal.toString());
                        UserContext.setUserId(userId);
                    }
                    Object roleVal = claims.get("role");
                    if (roleVal != null) {
                        UserContext.setUserRole(roleVal.toString());
                    }
                    UserContext.setUserEmail(claims.getSubject());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
