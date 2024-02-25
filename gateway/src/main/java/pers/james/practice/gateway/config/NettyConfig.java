package pers.james.practice.gateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * netty的相关参数
 */
@Component
@ConfigurationProperties(prefix = "netty")
@Getter
@Setter
@ToString
public class NettyConfig {

    private Integer port = 8080;
    /**
     * netty boss线程数量
     */
    private Integer bossGroupThreadCount = 1;
    /**
     * netty worker线程数量
     */
    private Integer workGroupThreadCount = 8;

    /**
     * 最大等待连接数
     */
    private Integer backLog = 128;

    /**
     * 禁用tcp的批量发送算法 Nagle
     */
    private Boolean tcpNoDelay;

    /**
     * 写数据的低水位线
     */
    private Integer writeBufferWaterMarkLow = 32768;

    /**
     * 写数据的高水位线
     */
    private Integer writeBufferWaterMarkHigh = 65536;

    /**
     * 读数据的空闲连接检测时间
     */
    private Integer readIdleTime;

    /**
     * http请求最大字节数
     */
    private Integer httpObjectMaxSize = 1024 * 1024;

    /**
     * 是否启用详细日志
     */
    private Boolean enableDetailLog = true;

}
