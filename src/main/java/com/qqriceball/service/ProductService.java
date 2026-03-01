package com.qqriceball.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.model.dto.ProductDTO;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.entity.Product;
import com.qqriceball.model.vo.ProductPageQueryVO;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.mapper.ProductOptionMapper;
import com.qqriceball.model.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductOptionMapper productOptionMapper;
    private final EmpMapper empMapper;

    @Autowired
    public ProductService(ProductMapper productMapper, ProductOptionMapper productOptionMapper, EmpMapper empMapper) {
        this.productMapper = productMapper;
        this.productOptionMapper = productOptionMapper;
        this.empMapper = empMapper;
    }

    @Transactional
    public ProductVO create(ProductDTO productDTO) {

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        try {
            //新增菜單品項
            productMapper.insert(product);
            Integer id = product.getId();

            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(product, productVO);
            return productVO;

        } catch (
                DuplicateKeyException e) {
            log.error("新增菜單名稱已存在,title: {}", product.getTitle(), e);
            throw new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXIST);
        }

    }

    public PageResult pageQuery(ProductPageQueryDTO productPageQueryDTO) {


        try{
            PageHelper.startPage(productPageQueryDTO.getPage(),
                    productPageQueryDTO.getPageSize());

            List<ProductPageQueryVO> list = productMapper.pageQuery(productPageQueryDTO);

            Page<ProductPageQueryVO> page = (Page<ProductPageQueryVO>) list;

            return new PageResult(page.getTotal(), productPageQueryDTO.getPage(),
                    productPageQueryDTO.getPageSize(), page.getResult());

        }catch (Exception e) {
            log.error("查詢異常：{}", productPageQueryDTO, e);
            throw new BadRequestArgsException(MessageEnum.BAD_REQUEST);
        }
    }

    public ProductVO getById(Integer id) {

        ProductVO productVO = productMapper.getById(id);

        if (productVO == null) {
            log.error("查無資料,ID: {}", id);
            throw new AccountNotExistException(MessageEnum.PRODUCT_NOT_EXIST);
        }else {
            return productVO;
        }
    }

}
