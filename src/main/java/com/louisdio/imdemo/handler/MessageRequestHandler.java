package com.louisdio.imdemo.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.louisdio.imdemo.IMServerApp;
import com.louisdio.imdemo.mapper.UserMapper;
import com.louisdio.imdemo.mapper.UserRelaMapper;
import com.louisdio.imdemo.pojo.User;
import com.louisdio.imdemo.pojo.UserRela;
import com.louisdio.imdemo.protobuf.MessageProtobuf;
import com.louisdio.imdemo.session.Session;
import com.louisdio.imdemo.session.SessionUtil;
import io.netty.channel.*;

import java.time.LocalDateTime;
import java.util.List;

public class MessageRequestHandler extends SimpleChannelInboundHandler<MessageProtobuf.Msg> {

    private UserMapper userMapper = null;
    private UserRelaMapper userRelaMapper = null;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtobuf.Msg msg) throws Exception {
        System.out.println("收到来自客户端的Msg：" + msg);
//        userMapper = IMServerApp.getBean(UserMapper.class);
        userMapper = (UserMapper)IMServerApp.getBean("userMapper");
        userRelaMapper = (UserRelaMapper)IMServerApp.getBean("userRelaMapper");
        MessageProtobuf.Head head = msg.getHead();
        MessageProtobuf.Msg.Builder returnMsgBuilder = MessageProtobuf.Msg.newBuilder();
        MessageProtobuf.Head.Builder returnHeadBuilder = MessageProtobuf.Head.newBuilder();
        Channel sendToChannel = ctx.channel();
        int msgType = head.getMsgType();
        String msgId = head.getMsgId();
        boolean toUserOnline = false;//标记接收方是否在线
        switch (msgType) {
            case 101://心跳
                heartbeat(returnMsgBuilder,returnHeadBuilder,msg);
//                returnHeadBuilder.setMsgType(101);
//                returnHeadBuilder.setFromId("server");
//                returnMsgBuilder.setBody("HEARTBEAT");
//                returnMsgBuilder.setHead(returnHeadBuilder.build());
                break;
            case 100://登录
//                String loginBody = msg.getBody();
//                System.out.println("loginBody:"+loginBody);
//                JSONObject loginJson = JSON.parseObject(loginBody);
//                String userCode = loginJson.getString("user_code");
//                String userPassword = loginJson.getString("user_password");
//                System.out.println("userCode:"+userCode);
//                System.out.println("userPassword:"+userPassword);
//
//                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//                userQueryWrapper.eq("user_code",userCode);
//                userQueryWrapper.eq("user_password",userPassword);
//
//                User user = userMapper.selectOne(userQueryWrapper);
//                if (user == null){//登录失败
//                    //TODO 登录失败
//                    returnMsgBuilder.setBody("LOGIN FAIL");
//                }else{
//                    returnMsgBuilder.setBody("LOGIN TOKEN");
//                }
//                System.out.println(userCode+" 登录成功");
//
//                returnHeadBuilder.setMsgType(100);
//                returnHeadBuilder.setFromId("server");
//                returnHeadBuilder.setExtend("{\"user_code\":\""+userCode+"\"}");
//                returnMsgBuilder.setHead(returnHeadBuilder.build());
                login(returnMsgBuilder,returnHeadBuilder,msg);
                Session fromSession = new Session(head.getFromId(), "");
                SessionUtil.bindSession(fromSession, ctx.channel());//为当前channel绑定session
                break;
            case 102://客户端相互通讯
                //1.取toUserId的channel
//                String fromId = head.getFromId();
                String toId = head.getToId();
                String body = msg.getBody();
//                //2.写数据
//                returnHeadBuilder.setMsgType(102);
//                returnHeadBuilder.setFromId(fromId);
//                returnHeadBuilder.setToId(toId);
//                returnMsgBuilder.setBody(body);
//                returnMsgBuilder.setHead(returnHeadBuilder.build());
                chat(returnMsgBuilder,returnHeadBuilder,msg);
                Channel toChannel = SessionUtil.getChannel(toId);
                if (toChannel == null) {
                    //TODO 对方不在线的消息处理逻辑
                    toUserOnline = false;
                    body = "OFFLINE";
                    //1.落库
                }else{
                    toUserOnline = true;
                    sendToChannel = toChannel;
                }
                break;
            case 104://查询好友列表
//                String userId = head.getFromId();
//                QueryWrapper<UserRela> friendsQueryWrapper = new QueryWrapper<>();
//                friendsQueryWrapper.eq("user_code",userId);
//                List<UserRela> userRela = userRelaMapper.selectList(friendsQueryWrapper);
//                System.out.println(userRela.toString());
//                JSONObject returnJsonObject = new JSONObject();
//                JSONArray list = new JSONArray();
//                if (userRela != null && userRela.size()>0){
//                    returnJsonObject.put("size",userRela.size());
//                    userRela.forEach(o -> {
//                        JSONObject obj = new JSONObject();
//                        obj.put("user_code",o.getUserFriendCode());
//                        obj.put("user_name","");
//                        obj.put("online",false);//TODO 获取channel，判断用户是否在线
//                        list.add(obj);
//                    });
//                }else{
//                    returnJsonObject.put("size",0);
//                }
//                returnJsonObject.put("list",list);
//                returnHeadBuilder.setMsgType(104);
//                returnHeadBuilder.setFromId("server");
////                returnHeadBuilder.setToId(toId);
//                returnMsgBuilder.setBody(returnJsonObject.toJSONString());
//                returnMsgBuilder.setHead(returnHeadBuilder.build());
                queryFriendList(returnMsgBuilder,returnHeadBuilder,msg);
                break;
            case 105://接收添加好友请求
                String friendUserCode = msg.getBody();
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.eq("user_code",friendUserCode);
                User friendUser = userMapper.selectOne(userQueryWrapper);
                if (friendUser==null){//好友用户不存在
                    returnMsgBuilder.setBody("FAIL");
                    return;
                }
                //查看好友是否在线
                Channel toFriendChannel = SessionUtil.getChannel(friendUserCode);
                //1.在线则向好友发送添加请求
                if (toFriendChannel == null) {
                    //TODO 对方不在线的添加请求处理逻辑
    //            toUserOnline = false;
    //            body = "OFFLINE";
                    //1.落库
                }else{
                    toUserOnline = true;
                    sendToChannel = toFriendChannel;
                    returnHeadBuilder.setMsgType(107);
                    returnHeadBuilder.setFromId(head.getFromId());
                    returnHeadBuilder.setToId(friendUserCode);
                    returnMsgBuilder.setBody("addFriend");
                    returnMsgBuilder.setHead(returnHeadBuilder.build());
                }
                break;
            case 106:
                queryAllFriendList(returnMsgBuilder,returnHeadBuilder,msg);
                break;
            case 108://收到同意添加好友请求
                String addFriendNotice = msg.getBody();
                JSONObject addFriendNoticeJson = JSON.parseObject(addFriendNotice);
                String userCode = addFriendNoticeJson.getString("user_code");
                String friendCode = addFriendNoticeJson.getString("add_friend");
                LocalDateTime now = LocalDateTime.now();
                UserRela userRela = new UserRela();
                userRela.setUserCode(userCode);
                userRela.setUserFriendCode(friendCode);
                userRela.setCreateTime(now);
                userRelaMapper.insert(userRela);
                UserRela friendRela = new UserRela();
                friendRela.setUserCode(friendCode);
                friendRela.setUserFriendCode(userCode);
                friendRela.setCreateTime(now);
                userRelaMapper.insert(friendRela);

                returnHeadBuilder.setFromId("server");
                returnHeadBuilder.setMsgType(108);
                returnMsgBuilder.setBody("SUCCESS");
                returnMsgBuilder.setHead(returnHeadBuilder.build());
                break;
        }
        MessageProtobuf.Msg returnMsg = returnMsgBuilder.build();
        System.out.println("回写客户端的Msg：" + returnMsg);
        ChannelFuture channelFuture = sendToChannel.writeAndFlush(returnMsg);
        boolean finalToUserOnline = toUserOnline;
        channelFuture.addListener(new ChannelFutureListener() {//监听写的结果
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (msgType == 102 && finalToUserOnline){//向其他客户端发送消息
                    //TODO 回写发送人
                    //发送消息后将消息暂存，已msgId为key，待收到服务端反馈后消除key，否则加入消息重发队列
                    String fromId = head.getFromId();
                    Channel fromChannel = SessionUtil.getChannel(fromId);
                    MessageProtobuf.Msg.Builder resultMsgBuilder = MessageProtobuf.Msg.newBuilder();
                    MessageProtobuf.Head.Builder resultHeadBuilder = MessageProtobuf.Head.newBuilder();
                    resultHeadBuilder.setMsgId(msgId);
                    resultHeadBuilder.setMsgType(103);
                    resultHeadBuilder.setFromId("server");
                    resultHeadBuilder.setToId(fromId);
                    if (future.isSuccess()){
                        resultMsgBuilder.setBody("SUCCESS");
                    }else{
                        resultMsgBuilder.setBody("FAIL");
                    }
                    resultMsgBuilder.setHead(resultHeadBuilder.build());
                    fromChannel.writeAndFlush(resultMsgBuilder.build());
                }else if(msgType == 100 || msgType == 101){
                    if (future.isSuccess()) {
                        System.out.println("write操作成功");
                    }else{
                        System.out.println("write操作失败");
                    }
                }else{

                }
//                ctx.close(); // 如果需要在write后关闭连接，close应该写在operationComplete中。注意close方法的返回值也是ChannelFuture
            }
        });
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive: client close");
    }


    /**
     * 客户端断开连接时执行
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        if (cause instanceof java.io.IOException) {
            System.out.println("exceptionCaught: client close");
        } else {
            cause.printStackTrace();
        }
    }

    /**
     * 心跳
     * @param returnMsgBuilder
     * @param returnHeadBuilder
     * @param msg
     */
    private void heartbeat(MessageProtobuf.Msg.Builder returnMsgBuilder,
                           MessageProtobuf.Head.Builder returnHeadBuilder,
                           MessageProtobuf.Msg msg
                           ){
        returnHeadBuilder.setMsgType(101);
        returnHeadBuilder.setFromId("server");
        returnMsgBuilder.setBody("HEARTBEAT");
        returnMsgBuilder.setHead(returnHeadBuilder.build());
    }

    /**
     * 登录
     * @param returnMsgBuilder
     * @param returnHeadBuilder
     * @param msg
     */
    private void login(MessageProtobuf.Msg.Builder returnMsgBuilder,
                       MessageProtobuf.Head.Builder returnHeadBuilder,
                       MessageProtobuf.Msg msg){
        String loginBody = msg.getBody();
        System.out.println("loginBody:"+loginBody);
        JSONObject loginJson = JSON.parseObject(loginBody);
        String userCode = loginJson.getString("user_code");
        String userPassword = loginJson.getString("user_password");
        System.out.println("userCode:"+userCode);
        System.out.println("userPassword:"+userPassword);

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_code",userCode);
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null){//注册
            User newUser = new User();
            newUser.setUserCode(userCode);
            newUser.setUserPassword(userPassword);
            newUser.setCreateTime(LocalDateTime.now());
            newUser.setUserName("");
            userMapper.insert(newUser);
            returnMsgBuilder.setBody("LOGIN TOKEN");
        }else if(user != null && !user.getUserPassword().equals(userPassword)){//登录失败
            returnMsgBuilder.setBody("LOGIN FAIL");
        }else{//登录成功
            returnMsgBuilder.setBody("LOGIN TOKEN");
        }

//        userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.eq("user_code",userCode);
//        userQueryWrapper.eq("user_password",userPassword);
//
//        User user = userMapper.selectOne(userQueryWrapper);
        System.out.println(userCode+" 登录成功");

        returnHeadBuilder.setMsgType(100);
        returnHeadBuilder.setFromId("server");
        returnHeadBuilder.setExtend("{\"user_code\":\""+userCode+"\"}");
        returnMsgBuilder.setHead(returnHeadBuilder.build());
    }

    /**
     * 聊天
     * @param returnMsgBuilder
     * @param returnHeadBuilder
     * @param msg
     */
    private void chat(MessageProtobuf.Msg.Builder returnMsgBuilder,
                      MessageProtobuf.Head.Builder returnHeadBuilder,
                      MessageProtobuf.Msg msg){
        //1.取toUserId的channel
        MessageProtobuf.Head head = msg.getHead();
        String fromId = head.getFromId();
        String toId = head.getToId();
        String body = msg.getBody();
        //2.写数据
        returnHeadBuilder.setMsgType(102);
        returnHeadBuilder.setFromId(fromId);
        returnHeadBuilder.setToId(toId);
        returnMsgBuilder.setBody(body);
        returnMsgBuilder.setHead(returnHeadBuilder.build());
    }

    /**
     * 查询朋友列表
     * @param returnMsgBuilder
     * @param returnHeadBuilder
     * @param msg
     */
    private void queryFriendList(MessageProtobuf.Msg.Builder returnMsgBuilder,
                                 MessageProtobuf.Head.Builder returnHeadBuilder,
                                 MessageProtobuf.Msg msg){
        MessageProtobuf.Head head = msg.getHead();
        String userId = head.getFromId();
        QueryWrapper<UserRela> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("user_code",userId);
        List<UserRela> userRela = userRelaMapper.selectList(friendsQueryWrapper);
        System.out.println(userRela.toString());
        JSONObject returnJsonObject = new JSONObject();
        JSONArray list = new JSONArray();
        if (userRela != null && userRela.size()>0){
            returnJsonObject.put("size",userRela.size());
            userRela.forEach(o -> {
                JSONObject obj = new JSONObject();
                obj.put("user_code",o.getUserFriendCode());
                obj.put("user_name","");
                obj.put("online",false);//TODO 获取channel，判断用户是否在线
                list.add(obj);
            });
        }else{
            returnJsonObject.put("size",0);
        }
        returnJsonObject.put("list",list);
        returnHeadBuilder.setMsgType(104);
        returnHeadBuilder.setFromId("server");
//                returnHeadBuilder.setToId(toId);
        returnMsgBuilder.setBody(returnJsonObject.toJSONString());
        returnMsgBuilder.setHead(returnHeadBuilder.build());
    }

    private void queryAllFriendList(MessageProtobuf.Msg.Builder returnMsgBuilder, MessageProtobuf.Head.Builder returnHeadBuilder, MessageProtobuf.Msg msg) {
        MessageProtobuf.Head head = msg.getHead();
        QueryWrapper<User> friendsQueryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(friendsQueryWrapper);

        System.out.println(users.toString());
        JSONObject returnJsonObject = new JSONObject();
        JSONArray list = new JSONArray();
        if (users != null && users.size()>0){
            returnJsonObject.put("size",users.size());
            users.forEach(o -> {
                JSONObject obj = new JSONObject();
                obj.put("user_code",o.getUserCode());
                obj.put("user_name","");
                obj.put("online",false);//TODO 获取channel，判断用户是否在线
                list.add(obj);
            });
        }else{
            returnJsonObject.put("size",0);
        }
        returnJsonObject.put("list",list);
        returnHeadBuilder.setMsgType(106);
        returnHeadBuilder.setFromId("server");
//                returnHeadBuilder.setToId(toId);
        returnMsgBuilder.setBody(returnJsonObject.toJSONString());
        returnMsgBuilder.setHead(returnHeadBuilder.build());
    }

    /**
     * 新增好友
     * @param returnMsgBuilder
     * @param returnHeadBuilder
     * @param msg
     */
    private void addFriend(MessageProtobuf.Msg.Builder returnMsgBuilder,
                           MessageProtobuf.Head.Builder returnHeadBuilder,
                           MessageProtobuf.Msg msg){
        String friendUserCode = msg.getBody();
        MessageProtobuf.Head head = msg.getHead();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_code",friendUserCode);
        User friendUser = userMapper.selectOne(userQueryWrapper);
        if (friendUser==null){//好友用户不存在
            returnMsgBuilder.setBody("FAIL");
            return;
        }
        //查看好友是否在线
        Channel toChannel = SessionUtil.getChannel(friendUserCode);
        //1.在线则向好友发送添加请求
        if (toChannel == null) {
            //TODO 对方不在线的消息处理逻辑
//            toUserOnline = false;
//            body = "OFFLINE";
            //1.落库
        }else{
//            toUserOnline = true;
//            sendToChannel = toChannel;
        }
        //2.不在线则保存请求，待好友登录时发送


    }
}
