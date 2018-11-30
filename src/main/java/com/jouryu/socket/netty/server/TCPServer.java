package com.jouryu.socket.netty.server;

import com.jouryu.socket.netty.handler.ReceiveSensorDataHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Created by tomorrow on 18/11/8.
 */

@Component
@Qualifier("tcpServer")
public class TCPServer extends AbstractNettyServer {

    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    @Autowired
    @Qualifier("receiveSensorDataHandler")
    private ReceiveSensorDataHandler receiveSensorDataHandler;

    TCPServer() {
        super.configName = "tcpServer";
    }

    @Override
    public void channelHandlerRegister(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 往pipeline中注册channelHandler
        pipeline.addLast(new ByteArrayEncoder());
        pipeline.addLast(new ByteArrayDecoder());
        pipeline.addLast(receiveSensorDataHandler);
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
