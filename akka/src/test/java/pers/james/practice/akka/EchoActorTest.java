package pers.james.practice.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

public class EchoActorTest {

    private static Logger log = LoggerFactory.getLogger(EchoActorTest.class);

    @Test
    public void createReceive() throws InterruptedException {
        ActorSystem system = ActorSystem.create("app");
        ActorRef echoActor = system.actorOf(Props.create(EchoActor.class), "echoActor");
        echoActor.tell("HAHA", ActorRef.noSender());
        echoActor.tell(1222L, echoActor);

        Future<Object> ask = Patterns.ask(echoActor, "echo me!", 2000);

        ask.foreach(v1 -> {
            log.info(v1.toString());
            return v1;
        }, system.dispatcher());

    }
}