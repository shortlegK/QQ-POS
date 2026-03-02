package com.qqriceball.unit.service;

import com.github.pagehelper.Page;
import com.qqriceball.common.exception.NotExistException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.integration.testData.product.SeedProductData;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.dto.ProductCreateDTO;
import com.qqriceball.model.dto.ProductEditDTO;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.entity.Product;
import com.qqriceball.model.vo.ProductPageQueryVO;
import com.qqriceball.model.vo.ProductVO;
import com.qqriceball.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("[Unit] ProductService.create - 建立菜單品項，應呼叫 productMapper.insert 傳入參數")
    void testCreateSuccess() {

        ProductCreateDTO productCreateDTO = new ProductCreateDTO();
        productCreateDTO.setTitle("測試品項");
        productCreateDTO.setProductType(ProductTypeEnum.MEAT.getCode());
        productCreateDTO.setPrice(100);
        productCreateDTO.setStatus(StatusEnum.ACTIVE.getCode());

        productService.create(productCreateDTO);

        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productArgumentCaptor.capture());

        Product captoredProduct = productArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(productCreateDTO.getTitle(), captoredProduct.getTitle(), "title 應與傳入參數相同"),
                () -> assertEquals(productCreateDTO.getProductType(), captoredProduct.getProductType(), "productType 應為加密後的密碼"),
                () -> assertEquals(productCreateDTO.getStatus(), captoredProduct.getStatus(), "status 應與傳入參數相同"),
                () -> assertEquals(productCreateDTO.getPrice(), captoredProduct.getPrice(), "price 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] ProductService.create - 建立重複菜單品項，應拋出 AlreadyExistsException")
    void testCreateProductTitleDuplicate() {

        ProductCreateDTO productCreateDTO = new ProductCreateDTO();
        productCreateDTO.setTitle("測試品項");
        productCreateDTO.setProductType(ProductTypeEnum.MEAT.getCode());
        productCreateDTO.setPrice(100);
        productCreateDTO.setStatus(StatusEnum.ACTIVE.getCode());

        doThrow(new DuplicateKeyException("duplicate"))
                .when(productMapper)
                .insert(any(Product.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> productService.create(productCreateDTO));
        assertEquals(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage(), ex.getMessage());

        verify(productMapper).insert(any(Product.class));
    }

    @Test
    @DisplayName("[Unit] ProductService.pageQuery - 分頁查詢成功，應回傳 PageResult 資料")
    void testPageQuerySuccess() {
        Integer page = 1;
        Integer pageSize = 5;
        String title = "product";

        ProductPageQueryDTO productPageQueryDTO = new ProductPageQueryDTO();
        productPageQueryDTO.setPage(page);
        productPageQueryDTO.setPageSize(pageSize);
        productPageQueryDTO.setTitle(title);

        ProductPageQueryVO data1 = new ProductPageQueryVO();
        data1.setId(1);
        data1.setTitle("product1");

        ProductPageQueryVO data2 = new ProductPageQueryVO();
        data2.setId(2);
        data2.setTitle("product2");

        Page<ProductPageQueryVO> mockPage = new Page<>(page, pageSize);
        mockPage.setTotal(2L);
        mockPage.add(data1);
        mockPage.add(data2);

        when(productMapper.pageQuery(any(ProductPageQueryDTO.class))).thenReturn(mockPage);

        PageResult result = productService.pageQuery(productPageQueryDTO);

        assertAll(
                () -> assertEquals(page, result.getPage()),
                () -> assertEquals(pageSize, result.getPageSize()),
                () -> assertEquals(2L, result.getTotal()),
                () -> assertEquals(mockPage.getResult(), result.getRecords())
        );

        verify(productMapper).pageQuery(any(ProductPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] ProductService.updateById - 更新菜單品項，應呼叫 productMapper.updateById 傳入參數")
    void testUpdateByIdSuccess() {

        ProductEditDTO productEditDTO = new ProductEditDTO();
        BeanUtils.copyProperties(SeedProductData.DRINK_PRODUCT, productEditDTO);

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(SeedProductData.DRINK_PRODUCT, productVO);

        when(productMapper.getById(any(Integer.class))).thenReturn(productVO);

        productService.updateById(productEditDTO);
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(productArgumentCaptor.capture());
        verify(productMapper, times(2)).getById(productEditDTO.getId());
        Product captoredProduct = productArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(productEditDTO.getId(), captoredProduct.getId(), "id 應與傳入參數相同"),
                () -> assertEquals(productEditDTO.getTitle(), captoredProduct.getTitle(), "title 應與傳入參數相同")
        );

    }

    @Test
    @DisplayName("[Unit] ProductService.updateById - 菜單品項 id 不存在，應拋出 NotExistException")
    void testUpdateByIdProductNotExist() {

        ProductEditDTO productEditDTO = new ProductEditDTO();
        BeanUtils.copyProperties(SeedProductData.DRINK_PRODUCT, productEditDTO);

        when(productMapper.getById(any(Integer.class))).thenReturn(null);

        NotExistException ex = assertThrows(NotExistException.class,
                () -> productService.updateById(productEditDTO));

        verify(productMapper).getById(productEditDTO.getId());
    }

    @Test
    @DisplayName("[Unit] ProductService.updateById - 欲修改菜單品項名稱已存在，應拋出 AlreadyExistsException")
    void testUpdateByIdProductTitleDuplicate() {
        ProductEditDTO productEditDTO = new ProductEditDTO();
        BeanUtils.copyProperties(SeedProductData.DRINK_PRODUCT, productEditDTO);

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(SeedProductData.DRINK_PRODUCT, productVO);

        when(productMapper.getById(any(Integer.class))).thenReturn(productVO);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(productMapper)
                .updateById(any(Product.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> productService.updateById(productEditDTO));
        assertEquals(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage(), ex.getMessage());

    }




}
