package pers.james.practice.gateway.ioc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pers.james.practice.gateway.disruptor.DisruptorConfiguration;
import pers.james.practice.gateway.web.NettyWebServer;

/**
 * spring 容器启动监听器
 */
@Component
@Slf4j
public class SpringIocCompletedListener implements CommandLineRunner {

    @Autowired
    private NettyWebServer nettyWebServer;

    @Autowired
    private DisruptorConfiguration disruptorConfiguration;

    @Override
    public void run(String... args) throws Exception {
        log.info("spring ioc completed... ");
        log.info("start disruptor...");
        disruptorConfiguration.initDisruptor();
        log.info("running netty server..");
        nettyWebServer.start();
        log.info("begin load api config...");
    }

}
