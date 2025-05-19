package com.hmdp.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisIdWorker {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final long BEGIN_TIMESTAMP = 1746949271L;

    // Uniform id
    public long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();

        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        String date =now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        // generate unique ID 
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix+":" + date);

        return  timestamp << 32 | count;
    }
}
