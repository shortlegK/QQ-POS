package com.qqriceball.unit.service;


import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.OptionCreateDTO;

import com.qqriceball.model.entity.Option;

import com.qqriceball.service.OptionService;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.utils.option.OptionTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OptionServiceTest {

    @Mock
    private OptionMapper optionMapper;

    @InjectMocks
    private OptionService optionService;

    @Test
    @DisplayName("[Unit] OptionService.create - 建立菜單細節品項，應呼叫 optionMapper.insert 傳入參數")
    void testCreateOptionSuccess() {

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);

        optionService.create(optionCreateDTO);

        ArgumentCaptor<Option> optionArgumentCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).insert(optionArgumentCaptor.capture());

        Option captoredOption = optionArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(optionCreateDTO.getTitle(), captoredOption.getTitle(), "title 應與傳入參數相同"),
                () -> assertEquals(optionCreateDTO.getOptionType(), captoredOption.getOptionType(), "productType 應為加密後的密碼"),
                () -> assertEquals(optionCreateDTO.getStatus(), captoredOption.getStatus(), "status 應與傳入參數相同"),
                () -> assertEquals(optionCreateDTO.getPrice(), captoredOption.getPrice(), "price 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] OptionService.create - 建立菜單細節品項名稱重複，應拋出 AlreadyExistsException")
    void testCreateOptionTitleDuplicate() {

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.EGG);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(optionMapper)
                .insert(any(Option.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> optionService.create(optionCreateDTO));
        assertEquals(MessageEnum.OPTION_ALREADY_EXISTS.getMessage(), ex.getMessage());

        verify(optionMapper).insert(any(Option.class));
    }
//
//    @Test
//    @DisplayName("[Unit] ProductService.pageQuery - 分頁查詢成功，應回傳 PageResult 資料")
//    void testPageQuerySuccess() {
//        Integer page = 1;
//        Integer pageSize = 5;
//        String title = "product";
//
//        ProductPageQueryDTO productPageQueryDTO = ProductTestDataFactory.getProductPageQueryDTO(page, pageSize, title);
//
//        ProductPageQueryVO data1 = ProductTestDataFactory.getProductPageQueryVO(SeedProductData.DRINK_PRODUCT);
//        ProductPageQueryVO data2 = ProductTestDataFactory.getProductPageQueryVO(SeedProductData.MEAT_PRODUCT);
//
//        Page<ProductPageQueryVO> mockPage = new Page<>(page, pageSize);
//        mockPage.setTotal(2L);
//        mockPage.add(data1);
//        mockPage.add(data2);
//
//        when(productMapper.pageQuery(any(ProductPageQueryDTO.class))).thenReturn(mockPage);
//
//        PageResult result = productService.pageQuery(productPageQueryDTO);
//
//        assertAll(
//                () -> assertEquals(page, result.getPage()),
//                () -> assertEquals(pageSize, result.getPageSize()),
//                () -> assertEquals(2L, result.getTotal()),
//                () -> assertEquals(mockPage.getResult(), result.getRecords())
//        );
//
//        verify(productMapper).pageQuery(any(ProductPageQueryDTO.class));
//    }
//
//    @Test
//    @DisplayName("[Unit] ProductService.updateById - 更新菜單品項，應呼叫 productMapper.updateById 傳入參數")
//    void testUpdateByIdSuccess() {
//
//        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.DRINK_PRODUCT);
//
//        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
//
//        when(productMapper.getById(any(Integer.class))).thenReturn(productVO);
//
//        productService.updateById(productEditDTO);
//        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
//        verify(productMapper).updateById(productArgumentCaptor.capture());
//        verify(productMapper, times(2)).getById(productEditDTO.getId());
//        Product captoredProduct = productArgumentCaptor.getValue();
//
//        assertAll(
//                () -> assertEquals(productEditDTO.getId(), captoredProduct.getId(), "id 應與傳入參數相同"),
//                () -> assertEquals(productEditDTO.getTitle(), captoredProduct.getTitle(), "title 應與傳入參數相同")
//        );
//
//    }
//
//    @Test
//    @DisplayName("[Unit] ProductService.updateById - 菜單品項 id 不存在，應拋出 NotExistException")
//    void testUpdateByIdProductNotExist() {
//
//        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.DRINK_PRODUCT);
//
//        when(productMapper.getById(any(Integer.class))).thenReturn(null);
//
//        assertThrows(NotExistException.class,
//                () -> productService.updateById(productEditDTO));
//
//        verify(productMapper).getById(productEditDTO.getId());
//    }
//
//    @Test
//    @DisplayName("[Unit] ProductService.updateById - 欲修改菜單品項名稱已存在，應拋出 AlreadyExistsException")
//    void testUpdateByIdProductTitleDuplicate() {
//        ProductEditDTO productEditDTO = ProductTestDataFactory.getProductEditDTO(SeedProductData.DRINK_PRODUCT);
//
//        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
//
//        when(productMapper.getById(any(Integer.class))).thenReturn(productVO);
//
//        doThrow(new DuplicateKeyException("duplicate"))
//                .when(productMapper)
//                .updateById(any(Product.class));
//
//        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
//                () -> productService.updateById(productEditDTO));
//        assertEquals(MessageEnum.PRODUCT_ALREADY_EXISTS.getMessage(), ex.getMessage());
//    }
//
//    @Test
//    @DisplayName("[Unit] ProductService.getById - 菜單品項 id 不存在，應拋出 NotExistException")
//    void  testGetByIdProductNotExist() {
//        Integer id = Integer.MAX_VALUE;
//
//        when(productMapper.getById(id)).thenReturn(null);
//
//        assertThrows(NotExistException.class,
//                () -> productService.getById(id));
//
//        verify(productMapper).getById(id);
//    }
//
//    @Test
//    @DisplayName("[Unit] ProductService.getById - 菜單品項 id 存在，應回傳 ProductVO 資料")
//    void testGetByIdProductExist() {
//        Integer id = SeedProductData.DRINK_PRODUCT.id();
//
//        ProductVO productVO = ProductTestDataFactory.getProductVO(SeedProductData.DRINK_PRODUCT);
//        when(productMapper.getById(any(Integer.class))).thenReturn(productVO);
//
//        ProductVO result = productService.getById(id);
//
//        assertAll(
//                () -> assertEquals(productVO.getId(), result.getId(), "id 應與傳入參數相同"),
//                () -> assertEquals(productVO.getTitle(), result.getTitle(), "title 應與傳入參數相同")
//        );
//        verify(productMapper).getById(id);
//    }

}
