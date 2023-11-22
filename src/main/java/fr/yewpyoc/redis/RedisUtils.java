package fr.yewpyoc.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisUtils {

    public static void setValue(StatefulRedisConnection<String, String> redisConnection, String key, String value) {
        RedisCommands<String, String> syncCommands = redisConnection.sync();
        syncCommands.set(key, value);
    }

    public static String getValue(StatefulRedisConnection<String, String> redisConnection, String key) {
        RedisCommands<String, String> syncCommands = redisConnection.sync();
        return syncCommands.get(key);
    }
}
