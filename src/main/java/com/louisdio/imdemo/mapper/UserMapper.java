package com.louisdio.imdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.louisdio.imdemo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserMapper extends BaseMapper<User> {


}
