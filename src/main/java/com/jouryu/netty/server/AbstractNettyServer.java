package com.jouryu.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Created by tomorrow on 18/11/8.
 */

abstract class AbstractNettyServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyServer.class);

    protected String configName;

    private Properties configProps;

    private Channel serverChannel;

    private InetSocketAddress socketrAdress;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private Integer backlog;

    private Boolean keepalive;

    private Boolean nodelay;

    public void startServer() throws Exception {
        configProps = new Properties();
        try {
            configProps.load(AbstractNettyServer.class.getClassLoader().getResourceAsStream("nettyserver.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        Integer port = Integer.valueOf(getProperty("port"));
        Integer bossCount = Integer.valueOf(getProperty("bossCount"));
        Integer workerCount = Integer.valueOf(getProperty("workerCount"));

        backlog = Integer.valueOf(getProperty("backlog"));
        keepalive = Boolean.parseBoolean(getProperty("keepalive"));
        nodelay = Boolean.parseBoolean(getProperty("nodelay"));
        socketrAdress = new InetSocketAddress(port);
        bossGroup = new NioEventLoopGroup(bossCount);
        workerGroup = new NioEventLoopGroup(workerCount);

        bootstrap();
    }

    @PreDestroy
    public void stop() throws Exception {
        serverChannel.close();
        serverChannel.parent().close();
    }

    private void bootstrap() throws Exception {

        // netty的NIO服务启动类
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // 指定生成channel的工厂类对象
                .option(ChannelOption.SO_BACKLOG, backlog) // 设置accept queue最大连接数
                .childOption(ChannelOption.SO_KEEPALIVE, keepalive) // 如果链接长时间不通信,会测试链接的状态
                .childOption(ChannelOption.TCP_NODELAY, nodelay) // 禁用nagle算法,保证低延迟
                .childHandler(new SocketChannelInitializer()); // 处理客户端请求的channel的IO

        serverChannel = serverBootstrap.bind(socketrAdress).sync().channel();
        logger.info("Netty server 启动成功! 端口: " + socketrAdress.getPort());
        serverChannel.closeFuture().sync();
    }

    public abstract void channelHandlerRegister(SocketChannel socketChannel);

    private class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {
        // 这个方法在Channel被注册到EventLoop的时候会被调用
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            channelHandlerRegister(socketChannel);
        }
    }

    private String getProperty(String name) {
        return configProps.getProperty(configName + "." + name);
    }
}
