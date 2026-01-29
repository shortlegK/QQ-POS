package com.qqriceball.mapper;

import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.entity.Product;
import com.qqriceball.model.vo.ProductPageQueryVO;
import com.qqriceball.annotation.AutoFill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    @AutoFill(value = OperationType.INSERT)
    void insert(Product product);

    List<ProductPageQueryVO> pageQuery(ProductPageQueryDTO productPageQueryDTO);

}
