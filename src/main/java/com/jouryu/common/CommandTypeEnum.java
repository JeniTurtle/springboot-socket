package com.jouryu.common;

/**
 * Created by tomorrow on 18/11/13.
 */

public enum CommandTypeEnum {
    REGISTER("7f","register"),
    REGISTER_SUCCESS("7f0000","register_success"),
    READ("03","read"),
    CLOSE("8f", "close"); // CLOSE项主要是在本地TestTCPClient测试用, 用来模拟关闭客户端请求

    private String command;
    private String operate;

    CommandTypeEnum(String command,String  operate){
        this.command = command;
        this.operate = operate;
    }

    public static String getOperate(String command) {
        for (CommandTypeEnum c : CommandTypeEnum.values()) {
            if (c.getCommand() == command) {
                return c.operate;
            }
        }
        return null;
    }

    public String getCommand() {
        return command;
    }

    public String getOperate() {
        return operate;
    }
}
