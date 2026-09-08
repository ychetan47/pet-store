package com.petstore.gateway.filter;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final MeterRegistry meterRegistry;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private static class TokenBucket {
        private final long capacity;
        private final double refillRatePerSecond;
        private double tokens;
        private long lastRefillTimestamp;

        public TokenBucket(long capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + (elapsed / 1000.0) * refillRatePerSecond);
                lastRefillTimestamp = now;
            }
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        EndpointPolicy policy = getPolicyForPath(path);
        if (policy == null) {
            return chain.filter(exchange);
        }

        String clientIp = resolveClientIp(request);
        String bucketKey = clientIp + ":" + policy.name;

        TokenBucket bucket = buckets.computeIfAbsent(bucketKey, k -> new TokenBucket(policy.capacity, policy.refillRatePerSecond));

        // Periodic eviction if map gets too large
        if (buckets.size() > 10000) {
            buckets.clear();
        }

        if (!bucket.tryConsume()) {
            meterRegistry.counter("rate_limit_exceeded_total", "path", policy.name).increment();
            logger.warn("Rate limit exceeded for IP {} on route {}", clientIp, path);

            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().set("Retry-After", "30");

            String errorJson = "{\"success\":false,\"code\":\"TOO_MANY_REQUESTS\",\"message\":\"Rate limit exceeded. Please slow down your requests.\"}";
            DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange);
    }

    private EndpointPolicy getPolicyForPath(String path) {
        if (path.startsWith("/api/admin")) {
            return null;
        }
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            return new EndpointPolicy("auth", 20, 5.0);
        } else if (path.startsWith("/api/orders")) {
            return new EndpointPolicy("orders", 50, 15.0);
        } else if (path.startsWith("/api/products")) {
            return new EndpointPolicy("products", 100, 50.0);
        }
        return null;
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null && remoteAddress.getAddress() != null
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown";
    }

    private static class EndpointPolicy {
        final String name;
        final long capacity;
        final double refillRatePerSecond;

        EndpointPolicy(String name, long capacity, double refillRatePerSecond) {
            this.name = name;
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
