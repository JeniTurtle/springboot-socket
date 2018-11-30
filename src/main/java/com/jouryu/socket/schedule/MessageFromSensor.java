package com.jouryu.socket.schedule;

import com.jouryu.socket.common.CommandTypeEnum;
import com.jouryu.socket.model.Sensor;
import com.jouryu.socket.common.CacheData;
import com.jouryu.socket.util.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by tomorrow on 18/11/9.
 *
 * 并行任务, 往传感器发送获取数据的消息
 */

@Component
@EnableScheduling
public class MessageFromSensor implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(MessageFromSensor.class);

    private static boolean loopStatus = true;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 创建一个定长线程池，支持定时及周期性任务执行。
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        taskRegistrar.setScheduler(pool);
    }

    /**
     * 每5秒往所有传感器客户端发送请求, 传感器接收到之后会发送数据
     */
    @Scheduled(fixedRate = 5000)
    public void pushMessage() {
        if (CacheData.monitorMap.size() < 1) {
            logger.info("等待传感器客户端连接...");
        } else {
            logger.info("往所有传感器[" + String.join(",", CacheData.getCurrentMonitorList()) + "]发送请求数据...");
        }

        CacheData.monitorMap.forEach((monitorCode, channelId) -> {
            CacheData.monitorList.forEach(monitor -> {
                if (monitor.getMonitorCode().equals(monitorCode)) {
                    Channel channel = CacheData.tcpChannelMap.get(channelId);
                    monitor.getSensors().forEach(sensor -> {
                        byte[] ret = migrateRequestMsg(sensor);
                        loopStatus = true;
                        channel.writeAndFlush(ret).addListener(new CustomChannelFutureListener());
                        // 由于往channel写入操作是异步的, 不搞成同步的话, 有时多条信息会合并成一条发送给客户端
                        while(loopStatus) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                logger.error(e.getMessage());
                            }
                        }
                    });
                }
            });
        });
    }

    private class CustomChannelFutureListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture future)
        throws Exception {
            loopStatus = false;
        }
    }

    /**
     * 生成往传感器发送请求数据的字节数组
     * @param sensor
     * @return
     */
    private byte[] migrateRequestMsg(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        sb.append(sensor.getSensorCode());
        sb.append(CommandTypeEnum.READ.getCommand());
        sb.append("00");
        sb.append(sensor.getRegister());
        sb.append("0001");
        byte[] bytes = Utils.hexStringToBytes(sb.toString());
        sb.append(Utils.getCRC(bytes));
        return Utils.hexStringToBytes(sb.toString());
    }
}
