package com.louisdio.imdemo.handler;

import com.louisdio.imdemo.protobuf.MessageProtobuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageResponseHandler extends SimpleChannelInboundHandler<MessageProtobuf.Msg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtobuf.Msg msg) throws Exception {
        System.out.println("收到来自服务端的消息：" + msg);
//        ctx.channel().writeAndFlush(msg);
    }
}
