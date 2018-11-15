package com.jouryu.model;

import lombok.Data;

/**
 * Created by tomorrow on 18/11/9.
 *
 * 传感器表
 *
 * 注: 在idea上使用lombok注解, 需要安装插件并设置annotation processing, 不然会提示找不到方法
 */

@Data
public class Sensor {
    private String sensorId;
    private String monitorId;
    private String sensorType;
    private String sensorName;
    private String sensorCode;
    private double upThreshold;
    private double lowThreshold;
    private int updateInterval;
    private String register;
    private double resolution;
    private Monitor monitor;
}
