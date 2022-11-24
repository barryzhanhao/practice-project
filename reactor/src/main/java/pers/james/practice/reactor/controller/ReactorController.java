package pers.james.practice.reactor.controller;


import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class ReactorController {

    @GetMapping("/reactor")
    public String get() {
        Flux.just("1", "2", "3").subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(4);
            }

            @Override
            public void onNext(String s) {
                log.info("ReactorController.onNext {}", s);
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage(), t);
            }

            @Override
            public void onComplete() {
                log.info("ReactorController.onComplete");
            }
        });

        return "111";
    }


}
