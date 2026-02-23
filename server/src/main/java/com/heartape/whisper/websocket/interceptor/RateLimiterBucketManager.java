package com.heartape.whisper.websocket.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class RateLimiterBucketManager {

    private final long capacity;
    private final long refill;
    private final long period;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refill, Duration.ofSeconds(period))
                .build();
        return Bucket
                .builder()
                .addLimit(limit)
                .build();
    }

    public boolean consume(String key, long tokens) {
        return buckets
                .computeIfAbsent(key, k -> createBucket())
                .tryConsume(tokens);
    }

    // 用于回收，生产可结合缓存清理或定时检查
    public void remove(String key) {
        buckets.remove(key);
    }

    public void checkAndClear() {

    }
}
