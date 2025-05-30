package pers.james.practice.fury.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.james.practice.fury.Foo;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class SampleController {

    static ThreadSafeFury fury;

    static {
        fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(true)
//                .withCompatibleMode(CompatibleMode.COMPATIBLE)
//                .withScopedMetaShare(false)
                .buildThreadSafeFury();

        fury.register(Foo.class,true);
//        fury.register(String.class,true);
//        fury.register(Integer.class,true);
    }

    @GetMapping("/hello")
    public String get() {
        System.out.println(fury.deserialize(fury.serialize("abc")));
        System.out.println(fury.deserialize(fury.serialize(List.of(1, 2, 3))));
        System.out.println(fury.deserialize(fury.serialize(Map.of("k1", 1, "k2", 2))));
        Foo foo = new Foo("f",1);
        byte[] serialize = fury.serialize(foo);
        Object deserialize = fury.deserialize(serialize);
        System.out.println(deserialize);
        return "2222";
    }

}
