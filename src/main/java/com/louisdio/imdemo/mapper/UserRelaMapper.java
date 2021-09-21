package com.louisdio.imdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.louisdio.imdemo.pojo.UserRela;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserRelaMapper extends BaseMapper<UserRela> {
}
