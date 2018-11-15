package com.jouryu.model;

import lombok.Data;

import java.util.List;

/**
 * Created by tomorrow on 18/11/9.
 *
 * 水文站表
 *
 * 注: 在idea上使用lombok注解, 需要安装插件并设置annotation processing, 不然会提示找不到方法
 */

@Data
public class Monitor {
    private String monitorId;
    private String monitorName;
    private String monitorCode;
    private List<Sensor> sensors;
}
