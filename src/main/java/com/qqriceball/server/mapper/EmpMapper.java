package com.qqriceball.server.mapper;

import com.qqriceball.pojo.entity.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmpMapper {

    @Select("select * from emp where username = #{username}")
    Emp getByUsername(String username);
}
