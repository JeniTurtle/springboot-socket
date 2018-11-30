package com.jouryu.socket.netty.server;

import com.jouryu.socket.netty.handler.WebSocketFrameHandler;
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
 * Created by tomorrow on 18/11/8.
 */

@Component
@Qualifier("webSocketServer")
@PropertySource(value= "classpath:/nettyserver.properties")
public class WebSocketServer extends AbstractNettyServer {

    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    @Value("${socketServer.socketUrlPath}")
    private String socketUrlPath;

    @Autowired
    @Qualifier("webSocketFrameHandler")
    private WebSocketFrameHandler webSocketFrameHandler;

    WebSocketServer() {
        super.configName = "socketServer";
    }

    @Override
    public void channelHandlerRegister(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 往pipeline中注册channelHandler
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(64*1024));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(socketUrlPath));
        pipeline.addLast(webSocketFrameHandler);
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
