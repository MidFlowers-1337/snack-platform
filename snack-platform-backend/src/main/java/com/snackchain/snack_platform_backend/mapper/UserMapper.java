package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(String username);
}