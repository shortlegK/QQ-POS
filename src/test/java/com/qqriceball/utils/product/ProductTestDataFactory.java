package com.qqriceball.utils.product;

import com.qqriceball.model.dto.ProductCreateDTO;
import com.qqriceball.model.dto.ProductEditDTO;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.vo.ProductVO;
import com.qqriceball.testData.product.TestProduct;
import org.springframework.beans.BeanUtils;

public class ProductTestDataFactory {

    public static ProductEditDTO getProductEditDTO(TestProduct product){
        ProductEditDTO productEditDTO = new ProductEditDTO();
        BeanUtils.copyProperties(product, productEditDTO);
        return productEditDTO;
    }

    public static ProductCreateDTO getProductCreateDTO(TestProduct product){
        ProductCreateDTO productCreateDTO = new ProductCreateDTO();
        BeanUtils.copyProperties(product, productCreateDTO);
        return productCreateDTO;
    }

    public static ProductPageQueryDTO getProductPageQueryDTO(Integer page, Integer pageSize, String title){
        ProductPageQueryDTO productPageQueryDTO = new ProductPageQueryDTO();
        productPageQueryDTO.setPage(page);
        productPageQueryDTO.setPageSize(pageSize);
        productPageQueryDTO.setTitle(title);
        return productPageQueryDTO;
    }

    public static ProductVO getProductVO(TestProduct product){
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);
        return productVO;
    }

}
