package com.jouryu.model;

import lombok.Data;

/**
 * Created by tomorrow on 18/11/9.
 *
 * 莱山水文站的假数据表
 *
 * 注: 在idea上使用lombok注解, 需要安装插件并设置annotation processing, 不然会提示找不到方法
 */

@Data
public class Simulator {
    private int id;
    private double flow;  // 流速
    private double ph;  // ph值
    private double dog;  // 溶氧
    private double codmn;  // 高锰酸盐
    private double nh3h;  // 氨氮
}
