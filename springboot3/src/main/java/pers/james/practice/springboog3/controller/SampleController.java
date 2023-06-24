package pers.james.practice.springboog3.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pers.james.practice.springboog3.internal.entity.UserPo;
import pers.james.practice.springboog3.internal.impl.UserService;

@Slf4j
@RestController
public class SampleController {

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public String get() {
        return "2222";
    }

    @GetMapping(value = "/user/{id}")
    public UserPo getUser(@PathVariable("id") Long id) {
        return userService.getUser(id);
    }
}
