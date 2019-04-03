package com.jouryu.socket.netty.handler;

import com.jouryu.socket.util.AesEncryptUtils;
import com.jouryu.socket.util.RandomName;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomorrow on 18/11/9.
 *
 * websocket测试用
 */

@Component
@Qualifier("stormSocketFrameHandler")
@ChannelHandler.Sharable
public class StormSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Value("${stormSocketServer.authToken}")
    private String authToken;

    private Map<String, String> nameList = new HashMap<>();

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ChannelGroup stormChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 每次接受到客户端传来的数据后触发
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel incoming = ctx.channel();
        String message = msg.text();

        if (message.startsWith("token:")) {  // 跟storm客户端做了一个简单的效验
            String token = message.split(":")[1];
            token = AesEncryptUtils.decrypt(token);

            // 如果效验成功, 将channel移到stormChannels的集合里
            if (token.equals(authToken)) {
                channels.remove(incoming);
                stormChannels.add(incoming);
            }
        } else if (stormChannels.contains(incoming)) {
            // 判断是storm客户端发送的数据, 那么转发给所有浏览器客户端
            channels.forEach(channel -> channel.writeAndFlush(new TextWebSocketFrame(message)));
            incoming.writeAndFlush(new TextWebSocketFrame("SocketServer数据接收成功!"));
        }
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
