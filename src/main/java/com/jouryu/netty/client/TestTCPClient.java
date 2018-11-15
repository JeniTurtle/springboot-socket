package com.jouryu.netty.client;

import com.jouryu.common.CatchData;
import com.jouryu.common.CommandTypeEnum;
import com.jouryu.model.Monitor;
import com.jouryu.model.Sensor;
import com.jouryu.model.Simulator;
import com.jouryu.util.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Created by tomorrow on 18/11/9.
 *
 * 测试用的client
 * 模拟传感器发送数据
 */

@Component
@Qualifier("testTcpClient")
@PropertySource(value= "classpath:/nettyserver.properties")
public class TestTCPClient {

    private static final Logger logger = LoggerFactory.getLogger(TestTCPClient.class);

    @Value("${tcpServer.host}")
    private String host;

    @Value("${tcpServer.port}")
    private int port;

    private String monitorCode = "0002";

    private Map<String, List<Double>> simulatorListMap = new HashMap<>();

    private Map<String, Iterator<Double>> simulatorIteratorMap = new HashMap<>();

    public void startClient() throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .remoteAddress(new InetSocketAddress(this.host, this.port)) // 绑定连接端口和host信息
                    .handler(new CustomChannelInitializer());

            ChannelFuture cf = b.connect().sync(); // 异步连接服务器

            cf.channel().closeFuture().sync(); // 异步等待关闭连接channel
            logger.warn("客户端关闭"); // 关闭完成
        } finally {
            group.shutdownGracefully().sync(); // 释放线程池资源
        }
    }

    private class CustomChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new ByteArrayEncoder());
            pipeline.addLast(new ByteArrayDecoder());
            pipeline.addLast(new EchoClientHandler());
            logger.warn("客户端连接成功");
        }
    }

    private class EchoClientHandler extends SimpleChannelInboundHandler<byte[]> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            // 第一接入连接时, 给客户端发送一个注册信息, 包含水文站编号
            channel.writeAndFlush(Utils.hexStringToBytes("7f" + monitorCode + "00000000"));
            // 初始化模拟数据
            initSimulatorData();
            logger.warn("客户端已接入连接");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
            Channel channel = ctx.channel();
            String message = Utils.bytesToHexString(msg);

            String registerSuccessCommand = CommandTypeEnum.REGISTER_SUCCESS.getCommand();
            String closeCommand = CommandTypeEnum.CLOSE.getCommand();
            String readCommand = CommandTypeEnum.READ.getCommand();

            logger.warn("服务端发来数据: " + message);

            if (message.startsWith(registerSuccessCommand)) {
                logger.warn("服务端初始化信息成功");
            } else if(message.startsWith(closeCommand)) {
                logger.warn("服务端要求关闭客户");
                ctx.close();
            } else if(message.substring(2).startsWith(readCommand)) {
                String sensorCode = message.substring(0, 2);
                List<Double> list = simulatorListMap.get(sensorCode);
                Iterator<Double> iterator = simulatorIteratorMap.get(sensorCode);
                if (!iterator.hasNext()) {
                    iterator = list.iterator();
                    simulatorIteratorMap.put(message, iterator);
                }
                Double value = iterator.next();
                byte[] responseMsg = migrateShamMsg(sensorCode, value);
                channel.writeAndFlush(responseMsg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        private Monitor getCurrentMonitor() {
            for (Monitor monitor : CatchData.monitorList) {
                if (monitor.getMonitorCode().equals(monitorCode)) {
                    return monitor;
                }
            }
            return null;
        }

        /**
         * 模拟传感器实际数据格式伪造的假数据
         * @param sensorCode
         * @param value
         * @return
         */
        private byte[] migrateShamMsg(String sensorCode, double value) {
            Monitor monitor = getCurrentMonitor();
            Sensor currentSensor = null;

            for (Sensor sensor : monitor.getSensors()) {
                if (sensor.getSensorCode().equals(sensorCode)) {
                    currentSensor = sensor;
                }
            }
            StringBuffer sb = new StringBuffer();
            sb.append(sensorCode);
            sb.append("0302");
            int number = (int) (value / currentSensor.getResolution());
            String hexStr = Integer.toHexString(number);
            String prefix = "";
            if (hexStr.length() < 4) {
                prefix = String.join("", Collections.nCopies(4 - hexStr.length(), "0"));
            }
            sb.append(prefix);
            sb.append(hexStr);
            sb.append("809d"); // 随便写的4位
            return Utils.hexStringToBytes(sb.toString());
        }

        private void initSimulatorData() {
            List<Double> flowList = new ArrayList<>();
            List<Double> phList = new ArrayList<>();
            List<Double> dogList = new ArrayList<>();
            List<Double> codmnList = new ArrayList<>();
            List<Double> nh3hList = new ArrayList<>();

            for (Simulator simulator : CatchData.simulatorList) {
                flowList.add(simulator.getFlow());
                phList.add(simulator.getPh());
                dogList.add(simulator.getDog());
                codmnList.add(simulator.getCodmn());
                nh3hList.add(simulator.getNh3h());
            }
            simulatorListMap.put("01", flowList);
            simulatorListMap.put("02", phList);
            simulatorListMap.put("03", dogList);
            simulatorListMap.put("04", codmnList);
            simulatorListMap.put("05", nh3hList);

            simulatorIteratorMap.put("01", flowList.iterator());
            simulatorIteratorMap.put("02", phList.iterator());
            simulatorIteratorMap.put("03", dogList.iterator());
            simulatorIteratorMap.put("04", codmnList.iterator());
            simulatorIteratorMap.put("05", nh3hList.iterator());
        }
    }
}
