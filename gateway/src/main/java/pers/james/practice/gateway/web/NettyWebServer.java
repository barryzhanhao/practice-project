package pers.james.practice.gateway.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.james.practice.gateway.config.NettyConfig;
import pers.james.practice.gateway.web.handler.HttpHandler;

/**
 * netty的web server
 */
@Component
@Slf4j
public class NettyWebServer {

    @Autowired
    private NettyConfig nettyConfig;

    @Autowired
    private HttpHandler httpHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;


    private volatile boolean isClosing = false;

    private static final String NETTY_RUN_THREAD_NAME = "netty-run-thread";

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void start() {
        Thread nettyStartThread = new Thread(new NettyStartTask(), NETTY_RUN_THREAD_NAME);
        nettyStartThread.start();
    }

    @PreDestroy
    public void stop() {
        if (!isClosing) {

            isClosing = true;

            log.info("shutting down all event loop");
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workGroup != null) {
                workGroup.shutdownGracefully();
            }
        }
    }


    private Class<? extends ServerChannel> determineServerSocketChannel() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        }

        return NioServerSocketChannel.class;
    }

    private void initEventLoop() {
        int bossCount = nettyConfig.getBossGroupThreadCount();
        int workCount = nettyConfig.getWorkGroupThreadCount();

        if (Epoll.isAvailable()) {
            log.info("epoll enabled");
            bossGroup = new EpollEventLoopGroup(bossCount, new DefaultThreadFactory("netty-boss"));
            workGroup = new EpollEventLoopGroup(workCount, new DefaultThreadFactory("netty-worker"));

        } else {
            bossGroup = new NioEventLoopGroup(bossCount, new DefaultThreadFactory("netty-boss"));
            workGroup = new NioEventLoopGroup(workCount, new DefaultThreadFactory("netty-worker"));
        }
    }

    private void startNetty() {
        log.info("Starting NettyWeb");
        initEventLoop();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(determineServerSocketChannel())
                    .option(ChannelOption.SO_BACKLOG, nettyConfig.getBackLog())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (nettyConfig.getEnableDetailLog()) {
                                p.addLast(createLoggingHandler());
                            }
                            p.addLast(new HttpServerCodec());
                            p.addLast("httpAggregator", new HttpObjectAggregator(nettyConfig.getHttpObjectMaxSize()));
                            p.addLast("decompressor", new HttpContentDecompressor());
                            p.addLast("httpServiceHandler", httpHandler);
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, nettyConfig.getTcpNoDelay())
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(nettyConfig.getWriteBufferWaterMarkLow(),
                            nettyConfig.getWriteBufferWaterMarkHigh()));

            ChannelFuture f = bootstrap.bind(nettyConfig.getPort()).sync();
            log.info("NettyWeb started at {}", nettyConfig.getPort());
            f.channel().closeFuture().sync();
            stop();
            log.info("NettyWeb stopped");

        } catch (Throwable e) {
            log.error("netty run fail", e);
            stop();
        }
    }

    private LoggingHandler createLoggingHandler() {
        LoggingHandler loggingHandler;
        if (log.isDebugEnabled()) {
            loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        } else if (log.isInfoEnabled()) {
            loggingHandler = new LoggingHandler(LogLevel.INFO);
        } else if (log.isErrorEnabled()) {
            loggingHandler = new LoggingHandler(LogLevel.ERROR);
        } else if (log.isTraceEnabled()) {
            loggingHandler = new LoggingHandler(LogLevel.TRACE);
        } else {
            loggingHandler = new LoggingHandler(LogLevel.WARN);
        }
        return loggingHandler;
    }

    private class NettyStartTask implements Runnable {

        @Override
        public void run() {
            startNetty();
        }

    }

}
