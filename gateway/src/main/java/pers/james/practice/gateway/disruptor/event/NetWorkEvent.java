package pers.james.practice.gateway.disruptor.event;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网络事件
 */
@Getter
@Setter
@ToString
public class NetWorkEvent {

    /**
     * http原始对象
     */
    private FullHttpRequest httpRequest;

    /**
     * http内容
     */
    private String httpBodyContent;


    /**
     * 是否已经被组件处理并返回了
     */
    private boolean hadReturn;

    /**
     * channel信息
     */
    private SocketChannel socketChannel;

    /**
     * 写数据的内存分配器
     */
    private ByteBufAllocator byteBufAllocator;

    /**
     * 开始处理请求的时间
     */
    private long startTime;

    public void clear() {
        this.hadReturn = false;
        this.socketChannel = null;
        this.byteBufAllocator = null;
        this.startTime = 0L;
    }

}
