package com.qqriceball.server.mapper;


import com.qqriceball.pojo.dto.EmpPageQueryDTO;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.pojo.vo.EmpPageQueryVO;
import com.qqriceball.pojo.vo.EmpVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmpMapper {

    @Select("select * from emp where username = #{username}")
    Emp getByUsername(String username);

    @Select("select id, username, name, role, status, entry_date from emp where id = #{id}")
    EmpVO getById(Integer id);

    void insert(Emp emp);

    List<EmpPageQueryVO> pageQuery(EmpPageQueryDTO empPageQueryDTO);

    void updateById(Emp emp);
}
