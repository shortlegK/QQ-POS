package com.qqriceball.unit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.controller.OptionController;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.model.dto.OptionCreateDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("[Unit] OptionController.createOption() - 建立產品細節選項，應回傳 200 及資料")
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

}
