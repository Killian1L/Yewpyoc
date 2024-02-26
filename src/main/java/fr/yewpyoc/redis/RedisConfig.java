package fr.yewpyoc.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisConfig {

    private final String REDIS_HOST = "redis-13767.c327.europe-west1-2.gce.cloud.redislabs.com";
    private final int REDIS_PORT = 13767;
    private final String REDIS_PASSWORD = "zQJidSbSMKtTv4PgoQvdjAZpeCLY0BiA";

    @Bean
    public Jedis jedis() {
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.auth(REDIS_PASSWORD);

        return jedis;
    }
}
