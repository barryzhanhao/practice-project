package pers.james.practice.gateway.web.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.james.practice.gateway.disruptor.DisruptorConfiguration;
import pers.james.practice.gateway.disruptor.event.NetWorkEvent;

import java.nio.charset.StandardCharsets;

/**
 * http协议的netty处理
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Autowired
    private DisruptorConfiguration disruptorConfiguration;

    private DisruptorEventProducer disruptorEventProducer;

    @PostConstruct
    public void init() {
        disruptorEventProducer = new DisruptorEventProducer();
    }

    @SuppressWarnings("AlibabaUndefineMagicConstant")
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        HttpMethod method = httpRequest.method();
        // 浏览器预检嗅探机制过滤
        if (method == HttpMethod.OPTIONS) {
            preCheckResponse(ctx);
            return;
        }
        // 读取http request中的数据
        // 这里的content是ByteBuf从池子里面分配的
        // 这里不读出来，方法结束之后 直接就会被释放掉了
        String httpBodyContent = "";
        if (httpRequest.content().readableBytes() > 0) {
            httpBodyContent = httpRequest.content().toString(StandardCharsets.UTF_8);
        }
        // event处理
        disruptorEventProducer.sendData(httpRequest,
                httpBodyContent,
                (SocketChannel) ctx.pipeline().channel(),
                ctx.alloc());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("netty get exception:", cause);
    }

    private class DisruptorEventProducer {

        public void sendData(FullHttpRequest httpRequest,
                             String httpBodyContent,
                             SocketChannel socketChannel,
                             ByteBufAllocator byteBufAllocator) {
            long sequence = disruptorConfiguration.getRingBuffer().next();
            try {
                NetWorkEvent event = disruptorConfiguration.getRingBuffer().get(sequence);
                if (event != null) {
                    event.setHttpRequest(httpRequest);
                    event.setHttpBodyContent(httpBodyContent);
                    event.setHadReturn(false);
                    event.setSocketChannel(socketChannel);
                    event.setStartTime(System.nanoTime() / 1000);
                    event.setByteBufAllocator(byteBufAllocator);
                }
            } finally {
                disruptorConfiguration.getRingBuffer().publish(sequence);
            }
        }
    }

    private void preCheckResponse(ChannelHandlerContext ctx) {
        String ok = "ok";
        ByteBuf buffer = ctx.alloc().buffer(ok.getBytes(StandardCharsets.UTF_8).length);
        buffer.writeBytes(ok.getBytes(StandardCharsets.UTF_8));
        // 写数据回channel
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK, buffer);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, ok.getBytes(StandardCharsets.UTF_8).length);
        // CORS
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        ctx.pipeline().channel().writeAndFlush(response);
    }

}
