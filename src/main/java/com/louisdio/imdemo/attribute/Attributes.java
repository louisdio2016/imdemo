package com.louisdio.imdemo.attribute;

import com.louisdio.imdemo.session.Session;
import io.netty.util.AttributeKey;

public interface Attributes {

    AttributeKey<Session> SESSION = AttributeKey.newInstance("session");
}
