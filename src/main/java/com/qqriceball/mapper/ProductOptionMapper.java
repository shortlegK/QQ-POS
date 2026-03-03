package com.qqriceball.mapper;

import com.qqriceball.model.entity.Option;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductOptionMapper {

    @Select("select id, title, option_type, price, status from product_option where id = #{id}")
    Option getById(Integer id);

}
