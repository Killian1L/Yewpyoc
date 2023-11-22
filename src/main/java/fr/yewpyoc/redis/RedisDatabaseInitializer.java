package fr.yewpyoc.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisDatabaseInitializer {

    private final StatefulRedisConnection<String, String> redisConnection;

    @Autowired
    public RedisDatabaseInitializer(StatefulRedisConnection<String, String> redisConnection) {
        this.redisConnection = redisConnection;
    }

    @PostConstruct
    public void init() {

    }
}
