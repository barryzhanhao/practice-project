package pers.james.practice.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class ToFindRemoteActorTest {

    @Test
    public void test() throws InterruptedException {
        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=2551")
            .withFallback(ConfigFactory.load("application.conf"));

        ActorSystem system = ActorSystem.create("sys", config);
        ActorRef toFind = system.actorOf(Props.create(ToFindRemoteActor.class), "toFind");

        TimeUnit.SECONDS.sleep(5);
    }

}