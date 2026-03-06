package com.qqriceball.unit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.NotExistException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.controller.OptionController;
import com.qqriceball.enumeration.DefaultEnum;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.model.dto.OptionCreateDTO;
import com.qqriceball.model.dto.OptionEditDTO;
import com.qqriceball.model.dto.OptionPageQueryDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.service.EmpService;
import com.qqriceball.service.OptionService;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.utils.option.OptionTestDataFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(OptionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OptionService optionService;

    @MockBean
    private JwtProperties jwtProperties;

    @MockBean
    EmpService empService;

    @Autowired
    private  ObjectMapper objectMapper;

    @BeforeEach
    void setUpAuth() {
        EmpVO emp = new EmpVO();
        emp.setId(99);
        Authentication auth = new UsernamePasswordAuthenticationToken(emp, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void cleanAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("[Unit] OptionController.createOption() - 建立產品細節選項成功，應回傳 200 及資料")
    void testCreateOptionSuccess() throws Exception {
        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);

        OptionVO optionVO = new OptionVO();
        BeanUtils.copyProperties(SeedOptionData.PURPLE_RICE, optionVO);
        when(optionService.create(any(OptionCreateDTO.class))).thenReturn(optionVO);

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(optionService).create(any(OptionCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.createOption() - 建立產品細節選項缺少必要參數，應回傳 400")
    void testCreateOptionMissingRequiredParameter() throws Exception {
        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        optionCreateDTO.setTitle(null);

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest());

        verify(optionService, never()).create(any(OptionCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.createOption() - 建立產品細節選項 OptionType 超過範圍，應回傳 400")
    void testCreateOptionInvalidOptionType() throws Exception {
        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        optionCreateDTO.setOptionType(4);

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest());

        verify(optionService, never()).create(any(OptionCreateDTO.class));
    }


    @Test
    @DisplayName("[Unit] OptionController.createOption() - 建立選項 OptionType 為 AddOn 預設設定錯誤，應回傳 400 及指定訊息")
    void testCreateOptionDefaultSettingError() throws Exception{

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.EGG);
        optionCreateDTO.setOptionType(OptionTypeEnum.ADD_ON.getCode());
        optionCreateDTO.setIsDefault(DefaultEnum.YES.getCode());

        doThrow(new BadRequestArgsException(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR))
                .when(optionService).create(any(OptionCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getMessage()));

        verify(optionService).create(any(OptionCreateDTO.class));

    }

    @Test
    @DisplayName("[Unit] OptionController.createOption() - 建立產品細節選項名稱重複，應回傳 409 及指定訊息")
    void testCreateOptionTitleDuplicate() throws Exception {
        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.EGG);

        doThrow(new AlreadyExistsException(MessageEnum.OPTION_ALREADY_EXISTS))
                .when(optionService).create(any(OptionCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ALREADY_EXISTS.getCode()));

        verify(optionService).create(any(OptionCreateDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.pageQueryOption() - 分頁查詢成功，應回傳 200 及資料")
    void testPageQueryOptionSuccess() throws Exception {

        OptionPageQueryDTO optionPageQueryDTO = OptionTestDataFactory.getOptionPageQueryDTO(1,10,null, OptionTypeEnum.ADD_ON.getCode(), null);

        List<OptionVO> mockData = new ArrayList<>();
        mockData.add(OptionTestDataFactory.getOptionVO(SeedOptionData.EGG));

        Long total = (long) mockData.size();
        PageResult mockResult = new PageResult(total,optionPageQueryDTO.getPage(),
                optionPageQueryDTO.getPageSize(), mockData);

        when(optionService.pageQuery(any(OptionPageQueryDTO.class))).thenReturn(mockResult);

        ResultActions resultActions = mockMvc.perform(
                get("/options/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", optionPageQueryDTO.getPage().toString())
                        .param("pageSize",optionPageQueryDTO.getPageSize().toString())
                        .param("optionType",String.valueOf(optionPageQueryDTO.getOptionType()))
        );

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(optionService).pageQuery(any(OptionPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.updateById() - 更新產品細節選項成功，應回傳 200 及資料")
    void testUpdateByIdSuccess() throws Exception {
        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.HOT_SPICY);

        OptionVO optionVO = OptionTestDataFactory.getOptionVO(SeedOptionData.HOT_SPICY);
        when(optionService.updateById(any(OptionEditDTO.class))).thenReturn(optionVO);

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(optionService).updateById(any(OptionEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.updateById() - 更新產品細節選項缺少必要參數，應回傳 400")
    void testUpdateByIdMissingRequiredParameter() throws Exception {
        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.HOT_SPICY);
        optionEditDTO.setId(null);

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest());

        verify(optionService, never()).updateById(any(OptionEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.updateById() - 更新產品細節選項 OptionType 超過範圍，應回傳 400")
    void testUpdateByIdInvalidOptionType() throws Exception {
        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.HOT_SPICY);
        optionEditDTO.setOptionType(4);

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest());

        verify(optionService, never()).updateById(any(OptionEditDTO.class));
    }


    @Test
    @DisplayName("[Unit] OptionController.updateById() - 更新選項 OptionType 為 AddOn 預設設定錯誤，應回傳 400 及指定訊息")
    void testUpdateByIdDefaultSettingError() throws Exception {
        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.EGG);
        optionEditDTO.setOptionType(OptionTypeEnum.ADD_ON.getCode());
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());

        doThrow(new BadRequestArgsException(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR))
                .when(optionService).updateById(any(OptionEditDTO.class));

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getMessage()));

        verify(optionService).updateById(any(OptionEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.updateById() - 更新 id 不存在，應回傳 404 及指定訊息")
    void testUpdateByIdNotExist() throws Exception {
        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.MILD_SPICY);

        doThrow(new NotExistException(MessageEnum.OPTION_NOT_EXIST))
                .when(optionService).updateById(any(OptionEditDTO.class));

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_NOT_EXIST.getCode()));

        verify(optionService).updateById(any(OptionEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.updateById() - 更新產品細節選項名稱重複，應回傳 409 及指定訊息")
    void testUpdateByIdOptionTitleDuplicate() throws Exception {

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.MILD_SPICY);

        doThrow(new AlreadyExistsException(MessageEnum.OPTION_ALREADY_EXISTS))
                .when(optionService).updateById(any(OptionEditDTO.class));

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ALREADY_EXISTS.getCode()));

        verify(optionService).updateById(any(OptionEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] OptionController.getById() - 查詢 id 不存在，應回傳 404 及指定訊息")
    void testGetByIdNotExist() throws Exception {
        Integer id = Integer.MAX_VALUE;

        doThrow(new NotExistException(MessageEnum.OPTION_NOT_EXIST))
                .when(optionService).getById(id);

        mockMvc.perform(
                get("/options/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_NOT_EXIST.getCode()));

        verify(optionService).getById(id);
    }

    @Test
    @DisplayName("[Unit] OptionController.getById() - 查詢 id 存在，應回傳 200 及資料")
    void testGetByIdExist() throws Exception {
        Integer id = SeedOptionData.EGG.id();

        OptionVO optionVO = OptionTestDataFactory.getOptionVO(SeedOptionData.EGG);
        when(optionService.getById(any(Integer.class))).thenReturn(optionVO);

        mockMvc.perform(
                get("/options/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
    }

}
