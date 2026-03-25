package com.qqriceball.unit.service;

import com.github.pagehelper.Page;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.model.dto.product.*;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.entity.Product;
import com.qqriceball.model.vo.ProductVO;
import com.qqriceball.service.ProductService;
import com.qqriceball.utils.product.ProductTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.List;

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
    @DisplayName("[Unit] ProductService.create() - 建立產品品項，應呼叫 productMapper.insert 傳入參數")
    void testCreateSuccess() {

        ProductCreateDTO productCreateDTO = ProductTestDataFactory.getProductCreateDTO(SeedProductData.MEAT_PRODUCT);

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
    @DisplayName("[Unit] ProductService.create() - 建立重複產品品項，應拋出 AlreadyExistsException")
    void testCreateProductTitleDuplicate() {

        ProductCreateDTO productCreateDTO = ProductTestDataFactory.getProductCreateDTO(SeedProductData.DRINK_PRODUCT);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(productMapper)
                .insert(any(Product.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> productService.create(productCreateDTO));
        assertEquals(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage(), ex.getMessage());

        verify(productMapper).insert(any(Product.class));
    }

    @Test
    @DisplayName("[Unit] ProductService.pageQuery() - 分頁查詢成功，應回傳 PageResult 資料")
    void testPageQuerySuccess() {
        Integer page = 1;
        Integer pageSize = 5;
        String title = "product";

        ProductPageQueryDTO productPageQueryDTO = ProductTestDataFactory.getProductPageQueryDTO(page, pageSize, title);

        ProductVO data1 = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        ProductVO data2 = ProductTestDataFactory.getProductVO(SeedProductData.MEAT_PRODUCT);

        Page<ProductVO> mockPage = new Page<>(page, pageSize);
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
    @DisplayName("[Unit] ProductService.updateById() - 更新產品品項，應呼叫 productMapper.updateById 傳入參數")
    void testUpdateByIdSuccess() {

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.DRINK_PRODUCT);

        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);

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
    @DisplayName("[Unit] ProductService.updateById() - 產品品項 id 不存在，應拋出 ResourceNotFoundException")
    void testUpdateByIdProductNotExist() {

        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.DRINK_PRODUCT);

        when(productMapper.getById(any(Integer.class))).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateById(productEditDTO));

        verify(productMapper).getById(productEditDTO.getId());
    }

    @Test
    @DisplayName("[Unit] ProductService.updateById() - 欲修改產品品項名稱已存在，應拋出 AlreadyExistsException")
    void testUpdateByIdProductTitleDuplicate() {
        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.DRINK_PRODUCT);

        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);

        when(productMapper.getById(any(Integer.class))).thenReturn(productVO);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(productMapper)
                .updateById(any(Product.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> productService.updateById(productEditDTO));
        assertEquals(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("[Unit] ProductService.getById() - 產品品項 id 不存在，應拋出 ResourceNotFoundException")
    void  testGetByIdProductNotExist() {
        Integer id = Integer.MAX_VALUE;

        when(productMapper.getById(id)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getById(id));

        verify(productMapper).getById(id);
    }

    @Test
    @DisplayName("[Unit] ProductService.getById() - 產品品項 id 存在，應回傳 ProductVO 資料")
    void testGetByIdProductExist() {
        Integer id = SeedProductData.DRINK_PRODUCT.id();

        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        when(productMapper.getById(any(Integer.class))).thenReturn(productVO);

        ProductVO result = productService.getById(id);

        assertAll(
                () -> assertEquals(productVO.getId(), result.getId(), "id 應與傳入參數相同"),
                () -> assertEquals(productVO.getTitle(), result.getTitle(), "title 應與傳入參數相同")
        );
        verify(productMapper).getById(id);
    }

    @Test
    @DisplayName("[Unit] ProductService.getActiveProductByType() - 查詢成功，應回傳資料")
    void testGetActiveProductByTypeSuccess() {

        ProductActiveQueryDTO productActiveQueryDTO = ProductTestDataFactory.getProductActiveQueryDTO(ProductTypeEnum.DRINKS.getCode());

        ProductVO data1 = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
        List<ProductVO> mockData = new ArrayList<>();
        mockData.add(data1);

        when(productMapper.getActiveProductByType(any(ProductActiveQueryDTO.class))).thenReturn(mockData);

        List<ProductVO> result = productService.getActiveProductByType(productActiveQueryDTO);

        assertEquals(mockData, result, "回傳資料應與 mock 資料相同");
        verify(productMapper).getActiveProductByType(any(ProductActiveQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] ProductService.updateStatus() - 更新產品上架狀態，應呼叫 ProductMapper.updateById 傳入參數")
    void testUpdateStatusSuccess(){
        Integer id = SeedProductData.DRINK_PRODUCT.id();
        ProductStatusDTO productStatusDTO = new ProductStatusDTO();
        productStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        ProductVO mockProduct = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);

        when(productMapper.getById(anyInt())).thenReturn(mockProduct);

        productService.updateStatus(id,productStatusDTO);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(productCaptor.capture());

        Product updateProduct = productCaptor.getValue();

        assertAll(
                () -> assertEquals(id,updateProduct.getId(),"id 應與傳入參數相同"),
                () -> assertEquals(productStatusDTO.getStatus(),updateProduct.getStatus(),"status 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] ProductService.updateStatus() - 更新產品上架狀態，id 不存在，應拋出 ResourceNotFoundException")
    void testUpdateStatusProductNotExist(){
        Integer id = Integer.MAX_VALUE;
        ProductStatusDTO productStatusDTO = new ProductStatusDTO();
        productStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        when(productMapper.getById(anyInt())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateStatus(id,productStatusDTO));

        verify(productMapper,never()).updateById(any(Product.class));
    }
}
