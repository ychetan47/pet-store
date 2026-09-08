package com.petstore.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${jwt.secret:dGhpcy1pcy1hLXNlY3VyZS1qd3Qtc2VjcmV0LWZvci1wZXQtc3RvcmUtdjEtZGV2ZWxvcG1lbnQtdGVzdC1rZXk=}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Allow preflight OPTIONS
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // 2. Allow public endpoints
        if (isPublicEndpoint(path, method)) {
            return chain.filter(exchange);
        }

        // 3. Extract Bearer token
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object userIdObj = claims.get("userId");
            String userId = userIdObj != null ? userIdObj.toString() : "";
            Object roleObj = claims.get("role");
            String role = roleObj != null ? roleObj.toString() : "CUSTOMER";
            String email = claims.getSubject() != null ? claims.getSubject() : "";

            // 4. Role-based authorization: Protect /api/admin/**
            if (path.startsWith("/api/admin")) {
                if (!"ADMIN".equalsIgnoreCase(role)) {
                    logger.warn("Access denied to admin path {} for user {} with role {}", path, email, role);
                    return onError(exchange, HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied: ADMIN role required");
                }
            }

            // 5. Forward identity headers to downstream microservices
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .header("X-User-Email", email)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception ex) {
            logger.warn("JWT validation failed for path {}: {}", path, ex.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid or expired JWT token");
        }
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return true;
        }

        if ("/api/auth/login".equals(path) || "/api/auth/register".equals(path)) {
            return true;
        }

        // GET requests to product and category browsing are public
        if (HttpMethod.GET.equals(method)) {
            if (path.startsWith("/api/products") && !path.startsWith("/api/admin/products")) {
                return true;
            }
            if (path.startsWith("/api/categories") && !path.startsWith("/api/admin/categories")) {
                return true;
            }
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format("{\"success\":false,\"code\":\"%s\",\"message\":\"%s\"}", code, message);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 3;
    }
}
