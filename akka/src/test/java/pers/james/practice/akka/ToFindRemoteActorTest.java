package pers.james.practice.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ToFindRemoteActorTest {

    @Test
    public void test() throws InterruptedException {
        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=2551");

        ActorSystem system = ActorSystem.create("sys", config);
        ActorRef toFind = system.actorOf(Props.create(ToFindRemoteActor.class), "toFind");

        Assertions.assertThat(toFind).isNotNull();
    }

}