package com.qqriceball.server.mapper;

import com.qqriceball.enumeration.OperationType;
import com.qqriceball.pojo.dto.ProductPageQueryDTO;
import com.qqriceball.pojo.entity.Product;
import com.qqriceball.pojo.vo.ProductPageQueryVO;
import com.qqriceball.server.annotation.AutoFill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    @AutoFill(value = OperationType.INSERT)
    void insert(Product product);

    List<ProductPageQueryVO> pageQuery(ProductPageQueryDTO productPageQueryDTO);

}
