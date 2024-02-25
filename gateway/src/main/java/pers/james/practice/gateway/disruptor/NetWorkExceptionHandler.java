package pers.james.practice.gateway.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import pers.james.practice.gateway.disruptor.event.NetWorkEvent;

/**
 * 默认的网络异常处理
 **/
@Slf4j
public class NetWorkExceptionHandler implements ExceptionHandler<NetWorkEvent> {


    @Override
    public void handleEventException(Throwable cause, long sequence, NetWorkEvent event) {


    }


    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("disruptor network start error", ex);
    }


    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("disruptor network shutdown error", ex);
    }

}