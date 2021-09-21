package com.louisdio.imdemo.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_rela")
public class UserRela {
    @TableId
    @TableField("user_code")
    private String userCode;
    @TableId
    @TableField("user_friendcode")
    private String userFriendCode;

    @TableField("create_time")
    private LocalDateTime createTime;

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserFriendCode() {
        return userFriendCode;
    }

    public void setUserFriendCode(String userFriendCode) {
        this.userFriendCode = userFriendCode;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
