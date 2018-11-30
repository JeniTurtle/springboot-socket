package com.jouryu.socket.netty.server;

import com.jouryu.socket.netty.handler.StormSocketFrameHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Created by tomorrow on 18/11/27.
 */

@Component
@Qualifier("stormSocketServer")
@PropertySource(value= "classpath:/nettyserver.properties")
public class StormSocketServer extends AbstractNettyServer {

    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    @Value("${stormSocketServer.socketUrlPath}")
    private String socketUrlPath;

    @Autowired
    @Qualifier("stormSocketFrameHandler")
    private StormSocketFrameHandler stormSocketFrameHandler;

    StormSocketServer() {
        super.configName = "stormSocketServer";
    }

    @Override
    public void channelHandlerRegister(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 往pipeline中注册channelHandler
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(64*1024));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(socketUrlPath));
        pipeline.addLast(stormSocketFrameHandler);
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
