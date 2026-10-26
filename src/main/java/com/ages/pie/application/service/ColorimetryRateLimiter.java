package com.ages.pie.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caps colorimetry analyses per user over a sliding window, since every analysis is a paid Bedrock call.
 * Counters live in memory: each running instance keeps its own window and a restart clears it.
 */
@Component
public class ColorimetryRateLimiter {

    static final int MAX_ANALYSES = 10;
    static final Duration WINDOW = Duration.ofHours(1);

    private final Map<UUID, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public ColorimetryRateLimiter() {
        this(Clock.systemUTC());
    }

    ColorimetryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Records an analysis for the user, or throws 429 when the window is already full. */
    public void acquire(UUID userId) {
        Instant now = clock.instant();
        Instant cutoff = now.minus(WINDOW);
        evictIdleUsers(cutoff);
        Instant[] retryAt = new Instant[1];
        attempts.compute(userId, (id, history) -> {
            Deque<Instant> recent = history == null ? new ArrayDeque<>() : history;
            prune(recent, cutoff);
            if (recent.size() >= MAX_ANALYSES) {
                retryAt[0] = recent.peekFirst().plus(WINDOW);
            } else {
                recent.addLast(now);
            }
            return recent;
        });
        if (retryAt[0] != null) {
            long minutes = Math.max(1, (Duration.between(now, retryAt[0]).toSeconds() + 59) / 60);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Você atingiu o limite de " + MAX_ANALYSES + " análises de colorimetria por hora. Tente novamente em "
                            + minutes + (minutes == 1 ? " minuto." : " minutos."));
        }
    }

    // Keeps the map bounded to users with analyses inside the current window.
    private void evictIdleUsers(Instant cutoff) {
        for (UUID id : attempts.keySet()) {
            attempts.computeIfPresent(id, (key, history) -> {
                prune(history, cutoff);
                return history.isEmpty() ? null : history;
            });
        }
    }

    private static void prune(Deque<Instant> history, Instant cutoff) {
        while (!history.isEmpty() && !history.peekFirst().isAfter(cutoff)) {
            history.pollFirst();
        }
    }

    int trackedUsers() {
        return attempts.size();
    }
}
