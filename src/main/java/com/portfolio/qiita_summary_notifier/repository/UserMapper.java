package com.portfolio.qiita_summary_notifier.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.portfolio.qiita_summary_notifier.entity.User;

@Mapper 
public interface UserMapper {

    void insertUser(User user);

    User selectById(@Param("id") Integer id);

    void updateUser(User user);

    void deleteUser(@Param("id") Integer id);
}
