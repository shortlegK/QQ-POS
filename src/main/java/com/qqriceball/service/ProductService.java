package com.qqriceball.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.model.dto.product.*;
import com.qqriceball.model.entity.Product;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.vo.product.ProductTypeVO;
import com.qqriceball.model.vo.product.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public ProductVO create(ProductCreateDTO productCreateDTO) {

        Product product = new Product();
        BeanUtils.copyProperties(productCreateDTO, product);

        try {
            //新增產品品項
            productMapper.insert(product);
            return productMapper.getById(product.getId());

        } catch(
                DuplicateKeyException e) {
            log.error("新增產品品項名稱已存在,title: {}", product.getTitle(), e);
            throw new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXISTS);
        }

    }

    public PageResult pageQuery(ProductPageQueryDTO productPageQueryDTO) {
        PageHelper.startPage(productPageQueryDTO.getPage(),
                productPageQueryDTO.getPageSize());

        List<ProductVO> list = productMapper.pageQuery(productPageQueryDTO);

        Page<ProductVO> page = (Page<ProductVO>) list;

        return new PageResult(page.getTotal(), productPageQueryDTO.getPage(),
                productPageQueryDTO.getPageSize(), page.getResult());
    }

    public ProductVO updateById(ProductEditDTO productEditDTO) {

        this.getById(productEditDTO.getId());

        Product product = new Product();
        BeanUtils.copyProperties(productEditDTO, product);
        try {
            productMapper.updateById(product);

            return productMapper.getById(product.getId());
        } catch (
                DuplicateKeyException e) {
            log.error("編輯產品品項名稱已存在,title: {}", product.getTitle(), e);
            throw new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXISTS);
        }

    }

    public ProductVO getById(Integer id) {

        ProductVO productVO = productMapper.getById(id);

        if (productVO == null) {
            log.error("查無資料,ID: {}", id);
            throw new ResourceNotFoundException(MessageEnum.PRODUCT_NOT_EXIST);
        }else {
            return productVO;
        }
    }

    public void updateStatus(Integer id, ProductStatusDTO productStatusDTO){
        this.getById(id);

        Product product = new Product();
        product.setId(id);
        product.setStatus(productStatusDTO.getStatus());
        productMapper.updateById(product);
    }

    public List<ProductTypeVO> getProductTypes(){
        List<ProductTypeVO> productTypes = new ArrayList<>();

        for(ProductTypeEnum typeEnum : ProductTypeEnum.values()){
            productTypes.add(new ProductTypeVO(typeEnum.getCode(), typeEnum.getDesc()));
        }
        return productTypes;
    }
}
