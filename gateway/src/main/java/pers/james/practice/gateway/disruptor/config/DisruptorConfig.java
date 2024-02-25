package pers.james.practice.gateway.disruptor.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * disruptor的配置
 */
@Component
@ConfigurationProperties(prefix = "disruptor")
@Getter
@Setter
@ToString
public class DisruptorConfig {

    /**
     * true->YieldingWaitStrategy
     * 自旋 + yield + 自旋
     * 性能和CPU资源之间有很好的折中。延迟比较均匀
     * 多用于生产环境
     * <p>
     * false->BlockingWaitStrategy
     * CPU资源紧缺，吞吐量和延迟并不重要的场景
     * 多用于测试环境
     */
    private boolean yieldingPolicy = false;

    /**
     * disruptor的环形队列长度
     * 生产环境可以一定规模调大 保证瞬间的大规模请求能够全部接入
     */
    private int ringBufferSize;

    /**
     * 网络事件处理者个数
     */
    private int networkEventProcessorCount = 10;

}
