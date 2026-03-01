package com.qqriceball.mapper;

import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.entity.Product;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.ProductPageQueryVO;
import com.qqriceball.annotation.AutoFill;
import com.qqriceball.model.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper {

    @AutoFill(value = OperationType.INSERT)
    void insert(Product product);

    List<ProductPageQueryVO> pageQuery(ProductPageQueryDTO productPageQueryDTO);

    @Select("select id, title, product_type, price, status from product where id = #{id}")
    ProductVO getById(Integer id);

    @Select("select * from product where title = #{title}")
    Product getByTitle(String title);

    @AutoFill(value = OperationType.UPDATE)
    void updateById(Product product);

}
