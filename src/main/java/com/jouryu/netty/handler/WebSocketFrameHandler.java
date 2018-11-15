package com.jouryu.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import com.jouryu.util.RandomName;

/**
 * Created by tomorrow on 18/11/9.
 *
 * websocket测试用
 */

@Component
@Qualifier("webSocketFrameHandler")
@ChannelHandler.Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private Map<String, String> nameList = new HashMap<>();

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 每次接受到客户端传来的数据后触发
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("客户端(" + nameList.get(incoming.id() + "") + ")发来信息: " + msg.text());
    }

    // 每次生成一个channel都会往pipeline注册handler,这时候就会触发这个方法
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String uName = new RandomName().getRandomName();
        Channel incoming = ctx.channel();
        channels.add(incoming);
        nameList.put(incoming.id() + "", uName);
        System.out.println("客户端(" + nameList.get(incoming.id() + "") + ")加入连接");
    }

    // pipeline注销handler时触发
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("客户端(" + nameList.get(incoming.id() + "") + ")终止连接");
        nameList.remove(incoming.id() + "");
        channels.remove(ctx.channel());
    }

    // 客户端连接成功后触发
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("客户端(" + nameList.get(incoming.id() + "") + ")成功连接");
    }

    // 客户端断开连接后触发
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("客户端(" + nameList.get(incoming.id() + "") + ")断开连接");
    }

    // 错误状态后触发
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("客户端(" + nameList.get(incoming.id() + "") + ")出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}
