package pers.james.practice.letture.controller;

import com.google.common.collect.Lists;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class LettureController {

    @GetMapping("/letture/sync")
    public String get(@RequestParam("value") String value) {
        String key = "key";
        RedisClient redisClient = RedisClient.create(RedisURI.builder().withHost("127.0.0.1").withPort(6379).build());
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> commands = connection.sync();
        commands.set(key, value);
        String str = commands.get(key);
        connection.close();
        redisClient.shutdown();
        return str;
    }

    @GetMapping("/letture/aysc")
    public String getAysc(@RequestParam("value") String value) throws ExecutionException, InterruptedException {
        String key = "key";
        RedisClient redisClient = RedisClient.create(RedisURI.builder().withHost("127.0.0.1").withPort(6379).build());
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> commands = connection.async();
        RedisFuture<String> set = commands.set(key, value);
        RedisFuture<String> get = commands.get(key);
        LettuceFutures.awaitAll(Duration.ZERO, set, get);
        String str = get.get();
        connection.close();
        redisClient.shutdown();
        return str;
    }

    @GetMapping("/letture/reactor")
    public String getAyscV2(@RequestParam("value") String value) throws ExecutionException, InterruptedException {
        String key = "key";
        RedisClient redisClient = RedisClient.create(RedisURI.builder().withHost("127.0.0.1").withPort(6379).build());
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisReactiveCommands<String, String> commands = connection.reactive();
        commands.set(key,value).subscribe(log::info);
        return "Ok";
    }

}
