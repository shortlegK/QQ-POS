package com.qqriceball.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qqriceball.common.exception.NotExistException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.model.dto.product.ProductCreateDTO;
import com.qqriceball.model.dto.product.ProductEditDTO;
import com.qqriceball.model.dto.product.ProductPageQueryDTO;
import com.qqriceball.model.entity.Product;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

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

        } catch (
                DuplicateKeyException e) {
            log.error("新增產品品項名稱已存在,title: {}", product.getTitle(), e);
            throw new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXISTS);
        }

    }

    public PageResult pageQuery(ProductPageQueryDTO productPageQueryDTO) {


        try{
            PageHelper.startPage(productPageQueryDTO.getPage(),
                    productPageQueryDTO.getPageSize());

            List<ProductVO> list = productMapper.pageQuery(productPageQueryDTO);

            Page<ProductVO> page = (Page<ProductVO>) list;

            return new PageResult(page.getTotal(), productPageQueryDTO.getPage(),
                    productPageQueryDTO.getPageSize(), page.getResult());

        }catch (Exception e) {
            log.error("查詢異常：{}", productPageQueryDTO, e);
            throw new BadRequestArgsException(MessageEnum.BAD_REQUEST);
        }
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
            throw new NotExistException(MessageEnum.PRODUCT_NOT_EXIST);
        }else {
            return productVO;
        }
    }

}
