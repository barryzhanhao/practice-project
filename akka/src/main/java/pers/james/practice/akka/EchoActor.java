package pers.james.practice.akka;

import akka.actor.AbstractActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, s -> {
            log.info("Received string msg {}, from {}", s, sender());
            if (!sender().isTerminated()) {
                sender().tell("Received :" + s, getSelf());
            }
        }).matchAny(o -> {
            log.info("Received unknown msg {}, class {}, from {}", o, o.getClass(), sender());
        }).build();
    }
}
