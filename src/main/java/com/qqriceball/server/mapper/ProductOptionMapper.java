package com.qqriceball.server.mapper;

import com.qqriceball.pojo.entity.ProductOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductOptionMapper {

    @Select("select id, title, option_type, price, status from product_option where id = #{id}")
    ProductOption getById(Integer id);

}
