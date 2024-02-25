package pers.james.practice.gateway.disruptor.factory;

import com.lmax.disruptor.EventFactory;
import pers.james.practice.gateway.disruptor.event.NetWorkEvent;

public class DisruptorEventFactory implements EventFactory<NetWorkEvent> {

    @Override
    public NetWorkEvent newInstance() {
        return new NetWorkEvent();
    }

}
