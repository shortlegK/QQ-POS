package com.qqriceball.server.mapper;

import com.qqriceball.enumeration.OperationType;
import com.qqriceball.pojo.entity.Product;
import com.qqriceball.server.annotation.AutoFill;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper {

    @AutoFill(value = OperationType.INSERT)
    void insert(Product product);

}
