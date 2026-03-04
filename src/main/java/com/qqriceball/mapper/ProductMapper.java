package com.qqriceball.mapper;

import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.entity.Product;
import com.qqriceball.annotation.AutoFill;
import com.qqriceball.model.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @AutoFill(value = OperationType.INSERT)
    void insert(Product product);

    List<ProductVO> pageQuery(ProductPageQueryDTO productPageQueryDTO);

    @Select("select id, title, product_type, price, status from products where id = #{id}")
    ProductVO getById(Integer id);

    @AutoFill(value = OperationType.UPDATE)
    void updateById(Product product);

}
