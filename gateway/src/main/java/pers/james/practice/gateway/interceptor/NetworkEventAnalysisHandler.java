package pers.james.practice.gateway.interceptor;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pers.james.practice.gateway.disruptor.event.NetWorkEvent;

/**
 * 抽象拦截器链
 */
@Scope("prototype")
@Component
@Slf4j
public class NetworkEventAnalysisHandler implements EventHandler<NetWorkEvent> {

    @Override
    public void onEvent(NetWorkEvent event, long sequence, boolean endOfBatch) throws Exception {
        log.info(event.toString());
    }


}
