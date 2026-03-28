package com.qqriceball.unit.service;


import com.github.pagehelper.Page;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.DefaultEnum;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.option.*;

import com.qqriceball.model.entity.Option;

import com.qqriceball.model.vo.option.OptionTypeVO;
import com.qqriceball.model.vo.option.OptionVO;
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
    @DisplayName("[Unit] OptionService.create() - 建立選項，其預設設定為「否」，不應清除同類型的預設選項設定")
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
    @DisplayName("[Unit] OptionService.create() - 建立選項，其預設設定為「是」，應同時設定同類型原預設選項的預設設定為「否」")
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
    void testCreateOptionAddOnDefaultSettingError() {

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
    @DisplayName("[Unit] OptionService.create() - 建立選項 OptionType 為 NoIngredient 預設設定錯誤，應拋出 BadRequestArgsException")
    void testCreateOptionNoIngredientDefaultSettingError() {

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.EGG);
        optionCreateDTO.setOptionType(OptionTypeEnum.NO_INGREDIENT.getCode());
        optionCreateDTO.setIsDefault(DefaultEnum.YES.getCode());

        BadRequestArgsException ex = assertThrows(BadRequestArgsException.class,
                () -> optionService.create(optionCreateDTO));

        assertEquals(MessageEnum.OPTION_NO_INGREDIENT_DEFAULT_ERROR.getMessage(), ex.getMessage());

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
    @DisplayName("[Unit] OptionService.updateById() - 更新選項，預設設定為「否」，不應清除同類型原預設選項設定")
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
    @DisplayName("[Unit] OptionService.updateById() - 更新選項，設定預設設定為「是」，應清除同類型原預設選項設定為「否」")
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
    @DisplayName("[Unit] OptionService.updateById() - 修改預設設定為「是」，同時修改 OptionType，應以修改後的 OptionType 進行預設設定檢查")
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
    @DisplayName("[Unit] OptionService.updateById() - 修改預設設定為「是」，未修改 OptionType，應以原 OptionType 進行預設設定檢查")
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
    @DisplayName("[Unit] OptionService.getById() - 產品細節選項 id 存在，應回傳 OptionVO 資料")
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
    @DisplayName("[Unit] OptionService.updateStatus - 修改產品細節選項上架狀態成功，應呼叫 OptionMapper.updateById() 傳入參數")
    void testUpdateOptionStatusSuccess() {
        Integer id = SeedOptionData.HOT_SPICY.id();
        OptionStatusDTO optionStatusDTO = new OptionStatusDTO();
        optionStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        OptionVO mockOption = OptionTestDataFactory.getOptionVO(SeedOptionData.HOT_SPICY);
        when(optionMapper.getById(id)).thenReturn(mockOption);

        optionService.updateStatus(id, optionStatusDTO);
        ArgumentCaptor<Option> optionCaptor = ArgumentCaptor.forClass(Option.class);
        verify(optionMapper).updateById(optionCaptor.capture());

        Option updatedOption = optionCaptor.getValue();

        assertAll(
                () -> assertEquals(id, updatedOption.getId(), "傳入的 id 應與呼叫 updateStatus 時的 id 相同"),
                () -> assertEquals(optionStatusDTO.getStatus(), updatedOption.getStatus(), "傳入的 status 應與呼叫 updateStatus 時的 status 相同")
        );
    }

    @Test
    @DisplayName("[Unit] OptionService.updateStatus - 修改產品細節選項 id 不存在，應拋出 ResourceNotFoundException")
    void testUpdateOptionStatusOptionNotExist() {
        Integer id = Integer.MAX_VALUE;
        OptionStatusDTO optionStatusDTO = new OptionStatusDTO();
        optionStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        when(optionMapper.getById(id)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> optionService.updateStatus(id, optionStatusDTO));

        verify(optionMapper).getById(id);
    }

    @Test
    @DisplayName("[Unit] OptionService.getOptionTypes() - 取得選項類型列表成功，應回傳所有選項類型資料")
    void testGetOptionTypesSuccess() {

        List<OptionTypeVO> result = optionService.getOptionTypes();

        assertEquals(OptionTypeEnum.values().length, result.size(), "回傳的選項類型數量應與 OptionTypeEnum 定義的數量相同");

        for (OptionTypeEnum typeEnum : OptionTypeEnum.values()){
            boolean found = false;

            for(OptionTypeVO vo : result){
                if(vo.getOptionType().equals(typeEnum.getCode()) && vo.getOptionTypeName().equals(typeEnum.getDesc())){
                    found = true;
                    break;
                }
            }
            assertTrue(found, typeEnum.getDesc()+" 類型應存在於回傳的選項類型列表中");
        }
    }

}
