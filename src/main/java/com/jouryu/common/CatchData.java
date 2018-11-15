package com.jouryu.common;

import com.jouryu.model.Monitor;
import com.jouryu.model.Sensor;
import com.jouryu.model.Simulator;
import com.jouryu.util.Utils;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tomorrow on 18/11/12.
 *
 * 保存服务器链接时的相关信息
 */

@Component
public final class CatchData {
    private CatchData() {}

    // 水文站和传感器对象, 每5分钟会有任务定时更新一次
    public static List<Monitor> monitorList;

    // 缓存传感器code和channle id的映射关系, key是传感器code, value是channel的id
    public static Map<String, String> monitorMap = new ConcurrentHashMap<>();

    // 缓存传感器连接的channel对象, key是channel的id, value是对应的channel对象
    public static Map<String, Channel> tcpChannelMap = new ConcurrentHashMap<>();

    // 莱山水文站模拟数据
    public static  List<Simulator> simulatorList;

    /**
     * 检查monitorCode是否存在于传感器列表中
     * @param code
     * @return
     */
    public static boolean containsMonitorCode(String code) {
        for (Monitor monitor: monitorList) {
            if (code.equals(monitor.getMonitorCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过monitorCode查询monitorName
     * @param monitorCode
     * @return
     */
    public static String getMonitorNameByCode(String monitorCode) {
        String monitorName = null;

        for (Monitor monitor: monitorList) {
            if (monitor.getMonitorCode().equals(monitorCode)) {
                monitorName = monitor.getMonitorName();
            }
        }
        return monitorName;
    }

    /**
     * 通过sensorCode获取对应的sensor对应
     * @param sensorCode
     * @return
     */
    public static Sensor getSensorBySensorCode(String sensorCode) {
        for (Monitor monitor: monitorList) {
            for (Sensor sensor : monitor.getSensors()) {
                if (sensor.getSensorCode().equals(sensorCode)) {
                    return sensor;
                }
            }
        }
        return null;
    }

    public static String getMonitorNameByChannelId(String channelId) {
        String monitorCode = Utils.getKeyByValue(monitorMap, channelId);
        return getMonitorNameByCode(monitorCode);
    }

    /**
     * 获取当前接入的设备列表
     * @return
     */
    public static List<String> getCurrentMonitorList() {
        List<String> list = new ArrayList<>();

        for (String monitorCode: monitorMap.keySet()) {
            monitorList.forEach(monitor -> {
                if (monitor.getMonitorCode().equals(monitorCode)) {
                    list.add(monitor.getMonitorName());
                }
            });
        }
        return list;
    }
}
