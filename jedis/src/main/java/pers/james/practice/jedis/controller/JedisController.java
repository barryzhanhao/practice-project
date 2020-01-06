package pers.james.practice.jedis.controller;

import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@RestController
public class JedisController {

    @GetMapping("/jedis-cluster")
    public String getJedisCluster() {
        JedisCluster jedisCluster = new JedisCluster(
            Set.of(HostAndPort.parseString("127.0.0.1:8000"), HostAndPort.parseString("127.0.0.1:8001"),
                HostAndPort.parseString("127.0.0.1:8002"), HostAndPort.parseString("127.0.0.1:8003"),
                HostAndPort.parseString("127.0.0.1:8004"), HostAndPort.parseString("127.0.0.1:8005")));
        jedisCluster.set("hello", "hahaah");
        return jedisCluster.get("hello");
    }
}
