package com.springboot.mapper;


import com.springboot.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

/**
 * Created by user on 2017/5/24.
 */
public interface UserMapper {

    User findUserById(@Param("id") Long id);

    int updateUsers(@Param("id") Long id);

    int updateUsersIds(@Param("id") Long id);

}
