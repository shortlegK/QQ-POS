package com.qqriceball.mapper;


import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.dto.EmpPageQueryDTO;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.vo.EmpPageQueryVO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.annotation.AutoFill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmpMapper {

    @Select("select * from emp where username = #{username}")
    Emp getByUsername(String username);

    @Select("select id, username, name, role, status, entry_date from emp where id = #{id}")
    EmpVO getById(Integer id);

    @AutoFill(value = OperationType.INSERT)
    void insert(Emp emp);

    List<EmpPageQueryVO> pageQuery(EmpPageQueryDTO empPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void updateById(Emp emp);
}
