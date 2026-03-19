package com.qqriceball.unit.service;


import com.github.pagehelper.Page;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.DefaultEnum;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.option.OptionActiveQueryDTO;
import com.qqriceball.model.dto.option.OptionCreateDTO;

import com.qqriceball.model.dto.option.OptionEditDTO;
import com.qqriceball.model.dto.option.OptionPageQueryDTO;
import com.qqriceball.model.entity.Option;

import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.service.OptionService;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.utils.option.OptionTestDataFactory;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OptionServiceTest {

    @Mock
    private OptionMapper optionMapper;

    @InjectMocks
    private OptionService optionService;

    @Test
    @DisplayName("Unit] OptionService.create() - 建立選項為「非預設選項」時不應清除同類型預設設定")
    void testCreateOptionIsDefaultNoSuccess() {

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        optionCreateDTO.setIsDefault(DefaultEnum.NO.getCode());

        optionService.create(optionCreateDTO);

        ArgumentCaptor<Option> optionArgumentCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).insert(optionArgumentCaptor.capture());
        verify(optionMapper,never()).cleanDefaultByOptionType(any(Integer.class));

        Option captoredOption = optionArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(optionCreateDTO.getTitle(), captoredOption.getTitle(), "title 應與傳入參數相同"),
                () -> assertEquals(optionCreateDTO.getOptionType(), captoredOption.getOptionType(), "productType 應為加密後的密碼"),
                () -> assertEquals(optionCreateDTO.getStatus(), captoredOption.getStatus(), "status 應與傳入參數相同"),
                () -> assertEquals(optionCreateDTO.getPrice(), captoredOption.getPrice(), "price 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] OptionService.create() - 建立選項為「預設選項」時應清除同類型其他選項的預設設定")
    void testCreateOptionIsDefaultYesSuccess() {

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        optionCreateDTO.setIsDefault(DefaultEnum.YES.getCode());

        optionService.create(optionCreateDTO);

        ArgumentCaptor<Option> optionArgumentCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).insert(optionArgumentCaptor.capture());
        verify(optionMapper).cleanDefaultByOptionType(optionCreateDTO.getOptionType());

        Option captoredOption = optionArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(optionCreateDTO.getTitle(), captoredOption.getTitle(), "title 應與傳入參數相同"),
                () -> assertEquals(optionCreateDTO.getOptionType(), captoredOption.getOptionType(), "productType 應為加密後的密碼"),
                () -> assertEquals(optionCreateDTO.getStatus(), captoredOption.getStatus(), "status 應與傳入參數相同"),
                () -> assertEquals(optionCreateDTO.getPrice(), captoredOption.getPrice(), "price 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] OptionService.create() - 建立選項 OptionType 為 AddOn 預設設定錯誤，應拋出 BadRequestArgsException")
    void testCreateOptionDefaultSettingError() {

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.EGG);
        optionCreateDTO.setOptionType(OptionTypeEnum.ADD_ON.getCode());
        optionCreateDTO.setIsDefault(DefaultEnum.YES.getCode());

        BadRequestArgsException ex = assertThrows(BadRequestArgsException.class,
                () -> optionService.create(optionCreateDTO));

        assertEquals(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getMessage(), ex.getMessage());

        verify(optionMapper, never()).cleanDefaultByOptionType(any(Integer.class));
        verify(optionMapper, never()).insert(any(Option.class));

    }

    @Test
    @DisplayName("[Unit] OptionService.create() - 建立產品細節選項名稱重複，應拋出 AlreadyExistsException")
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


    @Test
    @DisplayName("[Unit] OptionService.pageQuery() - 分頁查詢成功，應呼叫 OptionMapper.pageQuery() 並回傳分頁結果")
    void testPageQueryOptionSuccess(){

        Integer page = 1;
        Integer pageSize = 6;
        Integer optionType = OptionTypeEnum.SPICE_LEVEL.getCode();

        OptionPageQueryDTO optionPageQueryDTO = OptionTestDataFactory.getOptionPageQueryDTO(page, pageSize, null, optionType, null);

        OptionVO data1 = OptionTestDataFactory.getOptionVO(SeedOptionData.HOT_SPICY);
        OptionVO data2 = OptionTestDataFactory.getOptionVO(SeedOptionData.MEDIUM_SPICY);

        Page<OptionVO> mockPage = new Page<>(page,pageSize);
        mockPage.setTotal(2L);
        mockPage.add(data1);
        mockPage.add(data2);

        when(optionMapper.pageQuery(any(OptionPageQueryDTO.class))).thenReturn(mockPage);

        PageResult result = optionService.pageQuery(optionPageQueryDTO);

        assertAll(
                () -> assertEquals(page, result.getPage()),
                () -> assertEquals(pageSize, result.getPageSize()),
                () -> assertEquals(2L, result.getTotal()),
                () -> assertEquals(mockPage.getResult(), result.getRecords())
        );

        verify(optionMapper).pageQuery(any(OptionPageQueryDTO.class));
    }


    @Test
    @DisplayName("[Unit] OptionService.updateById() - 更新選項為「非預設選項」時不應清除同類型預設設定")
    void testUpdateByIdIsDefaultNoSuccess() {

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.LARGE_SIZE);
        optionEditDTO.setIsDefault(DefaultEnum.NO.getCode());

        OptionVO optionVO = OptionTestDataFactory.getOptionVO(SeedOptionData.LARGE_SIZE);

        when(optionMapper.getById(any(Integer.class))).thenReturn(optionVO);

        optionService.updateById(optionEditDTO);
        ArgumentCaptor<Option> optionArgumentCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).updateById(optionArgumentCaptor.capture());
        verify(optionMapper, times(2)).getById(optionEditDTO.getId());
        verify(optionMapper,never()).cleanDefaultByOptionType(any(Integer.class));

        Option captoredProduct = optionArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(optionEditDTO.getId(), captoredProduct.getId(), "id 應與傳入參數相同"),
                () -> assertEquals(optionEditDTO.getTitle(), captoredProduct.getTitle(), "title 應與傳入參數相同")
        );

    }

    @Test
    @DisplayName("[Unit] OptionService.updateById() - 更新選項為「預設選項」時應清除同類型預設設定")
    void testUpdateByIdIsDefaultYesSuccess() {

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.LARGE_SIZE);
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());

        OptionVO optionVO = OptionTestDataFactory.getOptionVO(SeedOptionData.LARGE_SIZE);

        when(optionMapper.getById(any(Integer.class))).thenReturn(optionVO);

        optionService.updateById(optionEditDTO);
        ArgumentCaptor<Option> optionArgumentCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).updateById(optionArgumentCaptor.capture());
        verify(optionMapper, times(2)).getById(optionEditDTO.getId());
        verify(optionMapper).cleanDefaultByOptionType(optionEditDTO.getOptionType());

        Option captoredProduct = optionArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(optionEditDTO.getId(), captoredProduct.getId(), "id 應與傳入參數相同"),
                () -> assertEquals(optionEditDTO.getTitle(), captoredProduct.getTitle(), "title 應與傳入參數相同")
        );

    }


    @Test
    @DisplayName("[Unit] OptionService.updateById() - 修改選項 OptionType 為 AddOn 預設設定錯誤，應拋出 BadRequestArgsException")
    void testUpdateByIdOptionDefaultSettingError() {

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.EGG);
        optionEditDTO.setOptionType(OptionTypeEnum.ADD_ON.getCode());
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());

        OptionVO optionVO = OptionTestDataFactory.getOptionVO(SeedOptionData.EGG);

        when(optionMapper.getById(any(Integer.class))).thenReturn(optionVO);

        BadRequestArgsException ex = assertThrows(BadRequestArgsException.class,
                () -> optionService.updateById(optionEditDTO));

        assertEquals(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getMessage(), ex.getMessage());

        verify(optionMapper, never()).cleanDefaultByOptionType(any(Integer.class));
        verify(optionMapper, never()).updateById(any(Option.class));

    }

    @Test
    @DisplayName("[Unit] OptionService.updateById() - 產品細節選項 id 不存在，應拋出 ResourceNotFoundException")
    void testUpdateByIdOptionNotExist() {

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.WHITE_RICE);

        when(optionMapper.getById(any(Integer.class))).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> optionService.updateById(optionEditDTO));

        verify(optionMapper).getById(optionEditDTO.getId());
    }

    @Test
    @DisplayName("[Unit] OptionService.updateById() - 欲修改產品細節選項名稱已存在，應拋出 AlreadyExistsException")
    void testUpdateByIdOptionTitleDuplicate() {
        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.COLD);

        OptionVO optionVO = OptionTestDataFactory.getOptionVO(SeedOptionData.COLD);

        when(optionMapper.getById(any(Integer.class))).thenReturn(optionVO);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(optionMapper)
                .updateById(any(Option.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> optionService.updateById(optionEditDTO));
        assertEquals(MessageEnum.OPTION_ALREADY_EXISTS.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("[Unit] OptionService.updateById() - 修改為「預設選項」時同時修改 OptionType，應以修改後的 OptionType 進行預設設定檢查")
    void testUpdateByIdIsDefaultYesWithOptionTypeChangeSuccess() {
        OptionEditDTO optionEditDTO = new OptionEditDTO();
        optionEditDTO.setId(SeedOptionData.LARGE_SIZE.id());
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());
        optionEditDTO.setOptionType(OptionTypeEnum.DRINK_TEMPERATURE.getCode());

        OptionVO originalData = OptionTestDataFactory.getOptionVO(SeedOptionData.LARGE_SIZE);

        when(optionMapper.getById(any(Integer.class))).thenReturn(originalData);

        optionService.updateById(optionEditDTO);
        ArgumentCaptor<Option> optionArgumentCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).updateById(optionArgumentCaptor.capture());
        verify(optionMapper, times(2)).getById(optionEditDTO.getId());
        verify(optionMapper).cleanDefaultByOptionType(optionEditDTO.getOptionType());
    }

    @Test
    @DisplayName("[Unit] OptionService.updateById() - 修改為「預設選項」時未修改 OptionType，應以原 OptionType 進行預設設定檢查")
    void testUpdateByIdIsDefaultYesWithoutOptionTypeChangeSuccess() {
        OptionEditDTO optionEditDTO = new OptionEditDTO();
        optionEditDTO.setId(SeedOptionData.LARGE_SIZE.id());
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());

        OptionVO originalData = OptionTestDataFactory.getOptionVO(SeedOptionData.LARGE_SIZE);

        when(optionMapper.getById(any(Integer.class))).thenReturn(originalData);
        optionService.updateById(optionEditDTO);
        ArgumentCaptor<Option> optionArgumentCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).updateById(optionArgumentCaptor.capture());
        verify(optionMapper, times(2)).getById(optionEditDTO.getId());
        verify(optionMapper).cleanDefaultByOptionType(originalData.getOptionType());
    }


    @Test
    @DisplayName("[Unit] OptionService.getById() - 產品細節選項 id 不存在，應拋出 ResourceNotFoundException")
    void  testGetByIdOptionNotExist() {
        Integer id = Integer.MAX_VALUE;

        when(optionMapper.getById(id)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> optionService.getById(id));

        verify(optionMapper).getById(id);
    }

    @Test
    @DisplayName("[Unit] OptionService.getById() - 產品細節選項 id 存在，應回傳 ProductVO 資料")
    void testGetByIdOptionExist() {
        Integer id = SeedProductData.DRINK_PRODUCT.id();

        OptionVO optionVO = OptionTestDataFactory.getOptionVO(SeedOptionData.MEDIUM_SPICY);
        when(optionMapper.getById(any(Integer.class))).thenReturn(optionVO);

        OptionVO result = optionService.getById(id);

        assertAll(
                () -> assertEquals(optionVO.getId(), result.getId(), "id 應與傳入參數相同"),
                () -> assertEquals(optionVO.getTitle(), result.getTitle(), "title 應與傳入參數相同")
        );
        verify(optionMapper).getById(id);
    }

    @Test
    @DisplayName("[Unit] OptionService.getActiveOptionsByType() - 查詢成功，應回傳資料")
    void testGetActiveOptionsByTypeSuccess() {

        OptionActiveQueryDTO optionActiveQueryDTO = OptionTestDataFactory.getOptionActiveQueryDTO(OptionTypeEnum.SPICE_LEVEL.getCode());

        List<OptionVO> mockData = new ArrayList<>();
        mockData.add(OptionTestDataFactory.getOptionVO(SeedOptionData.HOT_SPICY));
        mockData.add(OptionTestDataFactory.getOptionVO(SeedOptionData.MEDIUM_SPICY));

        when(optionMapper.getActiveOptionsByType(any(OptionActiveQueryDTO.class))).thenReturn(mockData);

        List<OptionVO> result = optionService.getActiveOptionsByType(optionActiveQueryDTO);

        assertEquals(mockData, result,"回傳資料應與 mock 資料相同");
        verify(optionMapper).getActiveOptionsByType(any(OptionActiveQueryDTO.class));
    }

}
