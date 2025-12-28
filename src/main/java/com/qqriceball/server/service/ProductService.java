package com.qqriceball.server.service;

import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.OptionNotFoundException;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.pojo.dto.ProductDTO;
import com.qqriceball.pojo.entity.Product;
import com.qqriceball.pojo.entity.ProductOptionLink;
import com.qqriceball.server.mapper.ProductMapper;
import com.qqriceball.server.mapper.ProductOptionLinkMapper;
import com.qqriceball.server.mapper.ProductOptionMapper;
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
    private final ProductOptionLinkMapper productOptionLinkMapper;

    @Autowired
    public ProductService(ProductMapper productMapper, ProductOptionMapper productOptionMapper, ProductOptionLinkMapper productOptionLinkMapper) {
        this.productMapper = productMapper;
        this.productOptionMapper = productOptionMapper;
        this.productOptionLinkMapper = productOptionLinkMapper;
    }

    @Transactional
    public void saveWithOption(ProductDTO productDTO) {

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        try {
            //新增菜單品項
            productMapper.insert(product);
        }catch (DuplicateKeyException e){
            log.error("新增菜單名稱已存在,title: {}",product.getTitle(),e);
            throw new AlreadyExistsException(MessageEnum.PRODUCT_ALEADY_EXIST);
        }

        //新增加料選項關聯
        Integer productId = product.getId(); // 取得剛才新增的品項 ID

        List<ProductOptionLink> productOptionLinks = productDTO.getProductOptionLinks();
        if(productOptionLinks != null && productOptionLinks.size() > 0) {
            productOptionLinks.forEach(link -> {
                if(productOptionMapper.getById(link.getOptionId()) != null) {
                    link.setProductId(productId);
                } else {
                    log.error("加料選項不存在,ID: {}", link.getOptionId());
                    throw new OptionNotFoundException(MessageEnum.OPTION_NOT_EXISTS);
                }
            });
            productOptionLinkMapper.insertBatch(productOptionLinks);
        }


    }

}
