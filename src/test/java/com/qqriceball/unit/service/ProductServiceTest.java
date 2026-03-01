package com.qqriceball.unit.service;

import com.github.pagehelper.Page;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.dto.ProductDTO;
import com.qqriceball.model.dto.ProductPageQueryDTO;
import com.qqriceball.model.entity.Product;
import com.qqriceball.model.vo.ProductPageQueryVO;
import com.qqriceball.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle("測試品項");
        productDTO.setProductType(ProductTypeEnum.MEAT.getCode());
        productDTO.setPrice(100);
        productDTO.setStatus(StatusEnum.ACTIVE.getCode());

        productService.create(productDTO);

        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).insert(productArgumentCaptor.capture());

        Product captoredProduct = productArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(productDTO.getTitle(), captoredProduct.getTitle(), "title 應與傳入參數相同"),
                () -> assertEquals(productDTO.getProductType(), captoredProduct.getProductType(), "productType 應為加密後的密碼"),
                () -> assertEquals(productDTO.getStatus(), captoredProduct.getStatus(), "status 應與傳入參數相同"),
                () -> assertEquals(productDTO.getPrice(), captoredProduct.getPrice(), "price 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] ProductService.create - 建立重複菜單品項，應拋出 AlreadyExistsException")
    void testCreateProductTitleDuplicate() {

        ProductDTO productDTO = new ProductDTO();
        productDTO.setTitle("測試品項");
        productDTO.setProductType(ProductTypeEnum.MEAT.getCode());
        productDTO.setPrice(100);
        productDTO.setStatus(StatusEnum.ACTIVE.getCode());

        doThrow(new DuplicateKeyException("duplicate"))
                .when(productMapper)
                .insert(any(Product.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> productService.create(productDTO));
        assertEquals(MessageEnum.PRODUCT_ALREADY_EXIST.getMessage(), ex.getMessage());

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


}
