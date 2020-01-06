package pers.james.practice.akka;

import akka.actor.AbstractActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ToFindRemoteActor extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        log.info("ToFindRemoteActor is starting");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, msg -> {
            log.info("Recieved string msg {}", msg);
        }).build();
    }
}
