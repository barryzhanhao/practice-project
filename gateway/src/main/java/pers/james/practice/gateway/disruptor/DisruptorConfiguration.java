package pers.james.practice.gateway.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.james.practice.gateway.disruptor.config.DisruptorConfig;
import pers.james.practice.gateway.disruptor.event.NetWorkEvent;
import pers.james.practice.gateway.disruptor.factory.DisruptorEventFactory;
import pers.james.practice.gateway.interceptor.NetworkEventAnalysisHandler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * disruptor配置
 */
@Component
@Slf4j
public class DisruptorConfiguration {

    @Autowired
    private DisruptorConfig disruptorConfig;

    @Autowired
    private ObjectFactory<NetworkEventAnalysisHandler> objectFactory;

    private volatile boolean inited = false;

    private RingBuffer<NetWorkEvent> ringBuffer;

    private Disruptor<NetWorkEvent> disruptor;

    public RingBuffer<NetWorkEvent> getRingBuffer() {
        if (!inited) {
            throw new RuntimeException("disruptor is not init..");
        }
        return ringBuffer;
    }

    public void initDisruptor() throws Exception {
        init();
        this.inited = true;
    }

    private void init() throws Exception {
        log.info("Starting Disruptor");
        if (disruptorConfig.getRingBufferSize() == 0) {
            disruptorConfig.setRingBufferSize(1024 * 1024);
        }
        Disruptor<NetWorkEvent> disruptorInstance = new Disruptor<>(
                new DisruptorEventFactory(),
                disruptorConfig.getRingBufferSize(),
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(0);

                    @Override
                    public Thread newThread(@SuppressWarnings("NullableProblems") Runnable r) {
                        int number = count.incrementAndGet();
                        String name = "disruptor-process-event-thread-" + number;
                        return new Thread(r, name);
                    }
                },
                ProducerType.MULTI,
                (disruptorConfig.isYieldingPolicy() ? new YieldingWaitStrategy() : new BlockingWaitStrategy())
        );

        // 编排消费者
        NetworkEventAnalysisHandler[] networkEventAnalysisHandlers = new NetworkEventAnalysisHandler[disruptorConfig.getNetworkEventProcessorCount()];
        for (int i = 0; i < disruptorConfig.getNetworkEventProcessorCount(); i++) {
            networkEventAnalysisHandlers[i] = objectFactory.getObject();
        }
        disruptorInstance.handleEventsWith(networkEventAnalysisHandlers);

        // 设置默认的异常处理
        disruptorInstance.setDefaultExceptionHandler(new NetWorkExceptionHandler());

        disruptorInstance.start();
        log.info("Disruptor started");
        this.ringBuffer = disruptorInstance.getRingBuffer();
        this.disruptor = disruptorInstance;
    }

    @PreDestroy
    public void destroyDisruptor() {
        if (disruptor != null) {
            log.info("stop disruptor");
            disruptor.shutdown();
        }
    }

}
