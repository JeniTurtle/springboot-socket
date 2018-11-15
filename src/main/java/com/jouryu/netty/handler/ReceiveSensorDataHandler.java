package com.jouryu.netty.handler;

import com.jouryu.common.CatchData;
import com.jouryu.common.CommandTypeEnum;
import com.jouryu.hbase.HbaseService;
import com.jouryu.model.Sensor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.jouryu.util.Utils;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tomorrow on 18/11/12.
 *
 * 处理传感器接收信息
 */

@Component
@Qualifier("receiveSensorDataHandler")
@PropertySource(value= "classpath:/application.properties")
@ChannelHandler.Sharable
public class ReceiveSensorDataHandler extends SimpleChannelInboundHandler<byte[]> {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveSensorDataHandler.class);

    @Value("${hbasePrefix}")
    private String hbasePrefix;

    @Value("${kafkaTopicPrefix}")
    private String kafkaTopicPrefix;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private HbaseService hbaseService;

    /**
     * 每次生成一个channel都会往pipeline注册handler,这时候就会触发这个方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 缓存当前连接的channel对象
        Channel channel = ctx.channel();
        CatchData.tcpChannelMap.put(getChannelId(channel), channel);
        logger.info("传感器(" + getRemoteAddress(ctx) + ")已加入处理队列");
    }

    /**
     * 客户端连接成功后触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("传感器(" + getRemoteAddress(ctx) + ")连接成功");
    }

    /**
     * 每次接受到客户端传来的数据后触发
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        Channel channel = ctx.channel();
        String message = Utils.bytesToHexString(msg);
        String channelId = getChannelId(channel);
        long datetime = new Date().getTime();

        // 传感器客户端第一次连接的时候, 会发送传感器相关信息
        if (message.startsWith(CommandTypeEnum.REGISTER.getCommand())) {
            logger.info("传感器(" + getRemoteAddress(ctx) + ")发来注册信息：" + message);
            registerHandler(channel, message);
        } else if (message.length() >= 14) {
            logger.info("传感器(" + getRemoteAddress(ctx) + ")[" + CatchData.getMonitorNameByChannelId(channelId) + "]发来数据：" + message);
            // 往hbase里写入数据
            dataHandler(channel, message, datetime);
            // 往kafka队列里写入数据
            kafkaHandler(channel, message, datetime);
            // 往websocket客户端发送实时数据
            websocketHanler(channel, message);
        }
    }

    /**
     * 每次接受到客户端传来的数据后触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String monitorCode = Utils.getKeyByValue(CatchData.monitorMap, getChannelId(channel));
        // 清除monitorMap对应的连接映射
        CatchData.monitorMap.remove(monitorCode);
        // 清除当前连接的channel对象
        CatchData.tcpChannelMap.remove(getChannelId(channel));
        logger.info("传感器(" + getRemoteAddress(ctx) + ")[" + CatchData.getMonitorNameByCode(monitorCode) + "]已退出处理队列");
        logger.info("当前传感器列表: [" + String.join(",", CatchData.getCurrentMonitorList()) + "]");
    }

    /**
     * 客户端断开连接后触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = getChannelId(ctx.channel());
        logger.info("传感器(" + getRemoteAddress(ctx) + ")[" + CatchData.getMonitorNameByChannelId(channelId) + "]断开连接");
    }

    /**
     * 错误状态后触发
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String channelId = getChannelId(ctx.channel());
        logger.info("传感器(" + getRemoteAddress(ctx) + ")[" + CatchData.getMonitorNameByChannelId(channelId) + "]发生异常");
        cause.printStackTrace();
        // 这里不能随便close, 不然设备那边没法自动重连, 得重启设备
        // ctx.close();
    }

    /**
     * 供本地调试使用, 模拟关闭测试的TestTCPClient
     * @param ctx
     * @return
     */
    public void testCloseClient(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String command = CommandTypeEnum.CLOSE.getCommand();
        channel.writeAndFlush(Utils.hexStringToBytes(command));
    }

    private static String getRemoteAddress(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString();
    }

    private static String getChannelId(Channel channel) {
        return channel.id() + "";
    }

    /**
     * 传感器第一连接的时候, 会把水文站编号发送过来
     * @param channel
     * @param message
     */
    private void registerHandler(Channel channel, String message) {
        // 获取传感器客户端code
        String monitorCode = message.substring(2, 6);
        String channelId = getChannelId(channel);

        // 如果客户端code不为空, 并且能在水文站列表中找到, 并且monitorMap中不存在
        if (!monitorCode.equals("")
                && CatchData.containsMonitorCode(monitorCode)
                && !CatchData.monitorMap.containsKey(monitorCode))
        {
            // 往客户端发送一条注册成功的消息
            String command = CommandTypeEnum.REGISTER_SUCCESS.getCommand();
            channel.writeAndFlush(Utils.hexStringToBytes(command));
            // 关联monitor code和channel id
            CatchData.monitorMap.put(monitorCode, channelId);
            logger.info("当前传感器列表: [" + String.join(",", CatchData.getCurrentMonitorList()) + "]");
        }
    }

    /**
     * 接收处理水质数据
     * @param channel
     * @param message
     */
    private void dataHandler(Channel channel, String message, long datetime) {
        String sensorCode = message.substring(0, 2);
        String value = getValue(message);
        String tableName = hbasePrefix + Utils.getKeyByValue(CatchData.monitorMap, getChannelId(channel));

        SimpleDateFormat rowTimeFormator = new SimpleDateFormat("yyyyMMddHH");
        SimpleDateFormat colTimeFormator = new SimpleDateFormat("yyyyMMddHHmmss");
        String hourTime = rowTimeFormator.format(datetime);
        String secondTime = colTimeFormator.format(datetime);

        List<Mutation> datas = new ArrayList<>();
        // rowkey为当前小时, 一小时内的数据都保存在一条记录中
        Put put = new Put(Bytes.toBytes(sensorCode + hourTime));
        put.addColumn(Bytes.toBytes("sensorInfo"), Bytes.toBytes("sensorType"), Bytes.toBytes(sensorCode));
        put.addColumn(Bytes.toBytes("sensorData"), Bytes.toBytes(secondTime), Bytes.toBytes(value));
        datas.add(put);
        List<Mutation> results = hbaseService.saveOrUpdate(tableName, datas);
        if (results.size() < 1) {
            logger.error("hbase数据库写入失败!");
        }
    }

    /**
     * 从接收到的数据中, 解析出检测的结果值
     * @param message
     * @return
     */
    private String getValue(String message) {
        String sensorCode = message.substring(0, 2);
        Sensor sensor = CatchData.getSensorBySensorCode(sensorCode);
        String value = message.replace(" ", "").substring(6, 10);
        Double number = Integer.parseInt(value, 16) * sensor.getResolution();
        return String.format("%.2f", number);
    }

    /**
     * 往kafka队列中写入数据
     * @param channel
     * @param message
     */
    private void kafkaHandler(Channel channel, String message, long datetime) {
        String sensorCode = message.substring(0, 2);
        String monitorCode = Utils.getKeyByValue(CatchData.monitorMap, getChannelId(channel));
        String topic = kafkaTopicPrefix + monitorCode;
        Sensor sensor = CatchData.getSensorBySensorCode(sensorCode);
        String msg = datetime + ":" + sensor.getSensorCode() + ":" + getValue(message);
        kafkaTemplate.send(topic, msg);
    }

    /**
     * 往websocket客户端发送实时数据
     * @param message
     */
    private void websocketHanler(Channel channel, String message) {
        String sensorCode = message.substring(0, 2);
        String value = getValue(message);
        String time = String.valueOf(System.currentTimeMillis());

        String monitorCode = Utils.getKeyByValue(CatchData.monitorMap, getChannelId(channel));
        String monitorName = CatchData.getMonitorNameByCode(monitorCode);

        Sensor sensor = CatchData.getSensorBySensorCode(sensorCode);
        String sensorName = sensor.getSensorName();
        String sensortype = sensor.getSensorType();
        Double low = sensor.getLowThreshold();
        Double up = sensor.getUpThreshold();

        String jsonData = "{\"msgType\":\"rawData\",\"siteCode\":\"" + monitorCode + "\",\"monitor\":\"" + monitorName + "\",\"sensortypename\":\"" + sensorName + "\",\"sensorCode\":\"" + sensortype + "\",\"value\":\"" + value + "\",\"timestamp\":\"" + time + "\"}";

        for (Channel websocketChannel : WebSocketFrameHandler.channels) {
            websocketChannel.writeAndFlush(new TextWebSocketFrame(jsonData));
        }

        if (Double.parseDouble(value) < low) {
            String warningData = "{\"msgType\":\"warning\",\"siteCode\":\"" + monitorCode + "\",\"monitor\":\"" + monitorName + "\",\"sensortypename\":\"" + sensorName + "\",\"sensorCode\":\"" + sensortype + "\",\"warningType\":\"值过低\"}";
            for (Channel websocketChannel : WebSocketFrameHandler.channels) {
                websocketChannel.writeAndFlush(new TextWebSocketFrame(warningData));
            }
        }

        if (Double.parseDouble(value) > up) {
            String warningData = "{\"msgType\":\"warning\",\"siteCode\":\"" + monitorCode + "\",\"monitor\":\"" + monitorName + "\",\"sensortypename\":\"" + sensorName + "\",\"sensorCode\":\"" + sensortype + "\",\"warningType\":\"值过高\"}";
            for (Channel websocketChannel : WebSocketFrameHandler.channels) {
                websocketChannel.writeAndFlush(new TextWebSocketFrame(warningData));
            }
        }
    }
}
