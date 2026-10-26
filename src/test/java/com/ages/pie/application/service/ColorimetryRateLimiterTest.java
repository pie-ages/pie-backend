package com.ages.pie.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColorimetryRateLimiterTest {
    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-25T12:00:00Z"));
    private final ColorimetryRateLimiter limiter = new ColorimetryRateLimiter(clock);
    private final UUID userId = UUID.randomUUID();

    @Test
    void allowsUpToTheLimitThenRejectsWithTooManyRequests() {
        exhaust(userId);

        assertThatThrownBy(() -> limiter.acquire(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS))
                .hasMessageContaining("limite de 10 análises")
                .hasMessageContaining("60 minutos");
    }

    @Test
    void freesASlotWhenTheOldestAnalysisLeavesTheWindow() {
        limiter.acquire(userId);
        clock.advance(Duration.ofMinutes(30));
        for (int i = 1; i < ColorimetryRateLimiter.MAX_ANALYSES; i++) {
            limiter.acquire(userId);
        }

        clock.advance(Duration.ofMinutes(29));
        assertThatThrownBy(() -> limiter.acquire(userId)).hasMessageContaining("1 minuto.");

        clock.advance(Duration.ofMinutes(1));
        assertThatCode(() -> limiter.acquire(userId)).doesNotThrowAnyException();
        assertThatThrownBy(() -> limiter.acquire(userId)).hasMessageContaining("30 minutos");
    }

    @Test
    void rejectedAttemptsDoNotExtendTheWindow() {
        exhaust(userId);
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> limiter.acquire(userId)).isInstanceOf(ResponseStatusException.class);
        }

        clock.advance(ColorimetryRateLimiter.WINDOW);
        assertThatCode(() -> limiter.acquire(userId)).doesNotThrowAnyException();
    }

    @Test
    void countsEachUserSeparately() {
        exhaust(userId);

        assertThatCode(() -> limiter.acquire(UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    void forgetsUsersWithoutAnalysesInTheWindow() {
        limiter.acquire(userId);
        clock.advance(ColorimetryRateLimiter.WINDOW);

        UUID other = UUID.randomUUID();
        limiter.acquire(other);

        assertThat(limiter.trackedUsers()).isEqualTo(1);
    }

    private void exhaust(UUID user) {
        for (int i = 0; i < ColorimetryRateLimiter.MAX_ANALYSES; i++) {
            limiter.acquire(user);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
