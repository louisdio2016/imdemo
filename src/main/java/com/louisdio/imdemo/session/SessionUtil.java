package com.louisdio.imdemo.session;

import com.louisdio.imdemo.attribute.Attributes;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionUtil {
    // userId -> channel 的映射
    private static final Map<String, Channel> userIdChannelMap = new ConcurrentHashMap<>();

    /**
     * 登录成功后为channel绑定session
     * 通过session获取用户信息
     * @param session
     * @param channel
     */
    public static void bindSession(Session session, Channel channel) {
        userIdChannelMap.put(session.getUserId(), channel);
        channel.attr(Attributes.SESSION).set(session);
        System.out.println("SessionUtil.bindSession  put session userId:"+session.getUserId());
    }

    public static void unBindSession(Channel channel) {
        if (hasLogin(channel)) {
            String userId = getSession(channel).getUserId();
            System.out.println("SessionUtil.bindSession  remove session userId:"+userId);
            userIdChannelMap.remove(userId);
            channel.attr(Attributes.SESSION).set(null);
        }
    }

    public static boolean hasLogin(Channel channel) {

        return channel.hasAttr(Attributes.SESSION);
    }

    public static Session getSession(Channel channel) {

        return channel.attr(Attributes.SESSION).get();
    }

    public static Channel getChannel(String userId) {

        return userIdChannelMap.get(userId);
    }
}
