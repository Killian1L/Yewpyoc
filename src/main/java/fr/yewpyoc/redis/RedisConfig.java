package fr.yewpyoc.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisConfig {

    // TODO : Redis document try for free redis db
    private final String REDIS_HOST = "redis-12323.c304.europe-west1-2.gce.cloud.redislabs.com";
    private final int REDIS_PORT = 12323;
    private final String REDIS_PASSWORD = "Rn97JtzHGZpdLoOUWhi0T2NNSe3o63sx";

    @Bean
    public Jedis jedis() {
//        String redisUrl = "redis://default:" + REDIS_PASSWORD + "@" + REDIS_HOST + ":" + REDIS_PORT;
//        Jedis jedis = new Jedis("redis://default:7OBTw6QnxRXK0zpZQLuGZfY4YnDdvRRQ@redis-15978.c304.europe-west1-2.gce.cloud.redislabs.com:15978");
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.auth(REDIS_PASSWORD);


        return jedis;
    }
}
