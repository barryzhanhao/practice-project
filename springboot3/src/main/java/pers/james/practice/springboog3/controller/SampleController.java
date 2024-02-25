package pers.james.practice.springboog3.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pers.james.practice.springboog3.internal.entity.DataVersionLogMongo;
import pers.james.practice.springboog3.internal.entity.UserPo;
import pers.james.practice.springboog3.internal.impl.UserService;
import pers.james.practice.springboog3.internal.repository.DataVersionLogMongoRepository;

@Slf4j
@RestController
public class SampleController {

    @Autowired
    private UserService userService;

    @Autowired
    private DataVersionLogMongoRepository dataVersionLogMongoRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/hello")
    public String get() {
        return "2222";
    }

    @GetMapping(value = "/user/{id}")
    public UserPo getUser(@PathVariable("id") Long id) {
        return userService.getUser(id);
    }

    @GetMapping(value = "/user2/{id}")
    public UserPo getUser2(@PathVariable("id") Long id) {
        return userService.getUser2(id);
    }

    @GetMapping(value = "/mongo")
    public DataVersionLogMongo mongo() {
        return dataVersionLogMongoRepository.get();
    }

    @GetMapping(value = "/redis/{key}")
    public String redis(@PathVariable("key") String key) {
        String value = redisTemplate.opsForValue().get(key);
        log.info("{}:{}", key, value);
        return value;
    }
}
