package com.qqriceball.mapper;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.entity.Option;
import com.qqriceball.model.vo.OptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OptionMapper {

    @Select("select id, title, option_type, price, status from options where id = #{id}")
    OptionVO getById(Integer id);

    @AutoFill(value = OperationType.INSERT)
    void  insert(Option option);

}
