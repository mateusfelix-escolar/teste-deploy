package br.org.edu.ifrn.LojaCarro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    @Test
    void shouldBlockRequestsFromSameOriginAfterLimitAndAllowAfterCooldown() throws ServletException, IOException {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimitFilter filter = new RateLimitFilter(clock, 20, Duration.ofMinutes(1));
        AtomicInteger chainInvocations = new AtomicInteger();

        FilterChain filterChain = (request, response) -> chainInvocations.incrementAndGet();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.0.10");

        for (int i = 0; i < 20; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus(), "Request " + (i + 1) + " should be allowed");
        }

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilter(request, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus());
        assertTrue(blockedResponse.getContentAsString().contains("Too many requests"));
        assertEquals(20, chainInvocations.get());

        clock.advance(Duration.ofMinutes(1));

        MockHttpServletResponse afterCooldownResponse = new MockHttpServletResponse();
        filter.doFilter(request, afterCooldownResponse, filterChain);

        assertEquals(200, afterCooldownResponse.getStatus());
        assertEquals(21, chainInvocations.get());
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
