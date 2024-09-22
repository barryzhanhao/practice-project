package pers.james.practice.log4j2.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SampleController {

    @GetMapping("/hello")
    public String get() {
        log.info("222222");
        return "2222";
    }

}
