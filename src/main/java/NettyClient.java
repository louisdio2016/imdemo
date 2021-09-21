import com.louisdio.imdemo.handler.MessageResponseHandler;
import com.louisdio.imdemo.protobuf.MessageProtobuf;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Scanner;

public class NettyClient {
    private static final int MAX_RETRY = 5;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8050;


    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ProtobufDecoder(MessageProtobuf.Msg.getDefaultInstance()));
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new MessageResponseHandler());
                    }
                });
        connect(bootstrap, HOST, PORT, MAX_RETRY);
    }

    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap.connect(HOST,PORT).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()){
                    Channel channel = ((ChannelFuture) future).channel();
                    startConsoleThread(channel);
                    System.out.println("连接成功!");
                }else{
                    System.out.println("连接失败!");
                }
            }
        });
    }

    private static void startConsoleThread(Channel channel){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    System.out.println("输入消息发送至服务端: ");
                    Scanner sc = new Scanner(System.in);
                    String line = sc.nextLine();
                    if (line.startsWith("login")){
                        //封装protoBuf
                        MessageProtobuf.Msg.Builder protoBufMessageBuilderByAppMessage = getProtoBufLoginBuilderByAppMessage(line);
                        channel.writeAndFlush(protoBufMessageBuilderByAppMessage.build());
                    }else if(line.startsWith("agree")){//同意朋友申请
                        //封装protoBuf
                        MessageProtobuf.Msg.Builder protoBufMessageBuilderByAppMessage = getProtoBufAgreeAddFriendBuilderByAppMessage(line);
                        channel.writeAndFlush(protoBufMessageBuilderByAppMessage.build());
                    }else if(line.startsWith("add")){//添加朋友
                        MessageProtobuf.Msg.Builder protoBufMessageBuilderByAppMessage = getProtoBufAddFriendBuilderByAppMessage(line);
                        channel.writeAndFlush(protoBufMessageBuilderByAppMessage.build());
                    }else{
                        //封装protoBuf
                        MessageProtobuf.Msg.Builder protoBufMessageBuilderByAppMessage = getProtoBufMessageBuilderByAppMessage(line);
                        channel.writeAndFlush(protoBufMessageBuilderByAppMessage.build());

                    }
                }
            }
        }).start();
    }

    public static MessageProtobuf.Msg.Builder getProtoBufMessageBuilderByAppMessage(String message){
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        headBuilder.setMsgType(102);
        headBuilder.setStatusReport(0);
        headBuilder.setMsgContentType(0);
        headBuilder.setMsgId("msgId");
        headBuilder.setFromId("mychild");
        headBuilder.setToId("wolf");
        headBuilder.setExtend("extend");

        builder.setBody(message);
        builder.setHead(headBuilder);
        return builder;
    }

    public static MessageProtobuf.Msg.Builder getProtoBufLoginBuilderByAppMessage(String message){
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        headBuilder.setMsgType(100);
        headBuilder.setStatusReport(0);
        headBuilder.setMsgContentType(0);
        headBuilder.setMsgId("msgId");
        headBuilder.setFromId("mychild");
        headBuilder.setToId("server");
        headBuilder.setExtend("Extend");

        builder.setBody("{'user_code':'mychild','user_password':'password'}");
        builder.setHead(headBuilder);
        return builder;
    }

    public static MessageProtobuf.Msg.Builder getProtoBufAgreeAddFriendBuilderByAppMessage(String message){
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        headBuilder.setMsgType(108);
        headBuilder.setStatusReport(0);
        headBuilder.setMsgContentType(0);
        headBuilder.setMsgId("msgId");
        headBuilder.setFromId("wolf");
        headBuilder.setToId("mychild");
        headBuilder.setExtend("Extend");

        builder.setBody("{'user_code':'wolf','add_friend':'mychild'}");
        builder.setHead(headBuilder);
        return builder;
    }
    public static MessageProtobuf.Msg.Builder getProtoBufAddFriendBuilderByAppMessage(String message){
        MessageProtobuf.Msg.Builder builder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder headBuilder = MessageProtobuf.Head.newBuilder();
        headBuilder.setMsgType(105);
        headBuilder.setStatusReport(0);
        headBuilder.setMsgContentType(0);
        headBuilder.setMsgId("msgId");
        headBuilder.setFromId("mychild");
        headBuilder.setToId("wolf");
        headBuilder.setExtend("Extend");

        builder.setBody("wolf");
        builder.setHead(headBuilder);
        return builder;
    }
}
