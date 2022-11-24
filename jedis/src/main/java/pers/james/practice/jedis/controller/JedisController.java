package pers.james.practice.jedis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.Set;

@RestController
public class JedisController {

    @GetMapping("/jedis-cluster")
    public String getJedisCluster() {
        JedisCluster jedisCluster = new JedisCluster(
                Set.of(HostAndPort.from("127.0.0.1:8000"), HostAndPort.from("127.0.0.1:8001"),
                        HostAndPort.from("127.0.0.1:8002"), HostAndPort.from("127.0.0.1:8003"),
                        HostAndPort.from("127.0.0.1:8004"), HostAndPort.from("127.0.0.1:8005")));
        jedisCluster.set("hello", "hahaah");
        return jedisCluster.get("hello");
    }
}
