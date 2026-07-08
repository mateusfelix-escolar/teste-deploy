package br.org.edu.ifrn.LojaCarro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final String TOO_MANY_REQUESTS_BODY = "{\"error\":\"Too many requests\",\"message\":\"Muitas requisições recebidas. Tente novamente em 1 minuto.\"}";

    private final Clock clock;
    private final int maxRequests;
    private final Duration blockDuration;
    private final Map<String, OriginState> requestsByOrigin = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        this(Clock.systemUTC(), 20, Duration.ofMinutes(1));
    }

    public RateLimitFilter(Clock clock, int maxRequests, Duration blockDuration) {
        this.clock = clock;
        this.maxRequests = maxRequests;
        this.blockDuration = blockDuration;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String origin = resolveOrigin(request);
        OriginState state = requestsByOrigin.computeIfAbsent(origin, key -> new OriginState());
        Instant now = clock.instant();

        if (state.isBlocked(now)) {
            reject(response);
            return;
        }

        if (state.count >= maxRequests) {
            state.blockedUntil = now.plus(blockDuration);
            state.count = 0;
            reject(response);
            return;
        }

        state.count++;
        filterChain.doFilter(request, response);
    }

    private String resolveOrigin(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(blockDuration.getSeconds()));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(TOO_MANY_REQUESTS_BODY);
    }

    private static class OriginState {
        private int count;
        private Instant blockedUntil;

        private boolean isBlocked(Instant now) {
            if (blockedUntil == null) {
                return false;
            }
            if (!now.isBefore(blockedUntil)) {
                blockedUntil = null;
                count = 0;
                return false;
            }
            return true;
        }
    }
}
