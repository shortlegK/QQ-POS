package com.qqriceball.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.OptionNotFoundException;
import com.qqriceball.common.exception.TypeNotFoundException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.model.dto.ProductDTO;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.entity.Product;
import com.qqriceball.model.entity.ProductOptionLink;
import com.qqriceball.model.vo.ProductPageQueryVO;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.mapper.ProductOptionLinkMapper;
import com.qqriceball.mapper.ProductOptionMapper;
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
            throw new AlreadyExistsException(MessageEnum.PRODUCT_ALREADY_EXIST);
        }

        //新增加料選項關聯
        Integer productId = product.getId(); // 取得剛才新增的品項 ID

        List<ProductOptionLink> productOptionLinks = productDTO.getProductOptionLinks();
        if(productOptionLinks != null && !productOptionLinks.isEmpty()) {
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

    //TODO: 分頁查詢，加料選項關聯查詢
    public PageResult pageQuery(ProductPageQueryDTO productPageQueryDTO) {

        try(Page<ProductPageQueryVO> pageQuery =
                    PageHelper.startPage(productPageQueryDTO.getPage(),
                            productPageQueryDTO.getPageSize())){

            productMapper.pageQuery(productPageQueryDTO);

            Long total = pageQuery.getTotal();
            List<ProductPageQueryVO> records = pageQuery.getResult();

            records.forEach(record -> {
                ProductTypeEnum typeEnum = ProductTypeEnum.getByCode(record.getProductType());
                if(typeEnum == null){
                    log.error("找不到對應的產品分類,type: {}", record.getProductType());
                    throw new TypeNotFoundException(MessageEnum.TYPE_NOT_FOUND);
                }else record.setTypeName(typeEnum.getDesc());
            });

            return new PageResult(total, productPageQueryDTO.getPage(), productPageQueryDTO.getPageSize(), records);

        }catch (Exception e){
            log.error("查詢異常：{}",productPageQueryDTO,e);
            throw new BadRequestArgsException(MessageEnum.BAD_REQUEST);
        }
    }

//    @Transactional
//    public void deleteBatch(List<Integer> ids) {
//        ids.forEach(id -> {
//            // 判斷是否啟用中
//
//            // 刪除菜單品項
//
//            // 刪除加料項目關聯
//        }
//    }
}
