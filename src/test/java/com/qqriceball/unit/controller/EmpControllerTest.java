package com.qqriceball.unit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.NotExistException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.model.dto.emp.EmpCreateDTO;
import com.qqriceball.model.dto.emp.EmpEditDTO;
import com.qqriceball.model.dto.emp.EmpPageQueryDTO;
import com.qqriceball.model.dto.emp.EmpStatusDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.controller.EmpController;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.service.EmpService;
import com.qqriceball.utils.emp.EmpTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.security.core.Authentication;


import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(EmpController.class)
@AutoConfigureMockMvc(addFilters = false)

public class EmpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmpService empService;

    @MockBean
    private JwtProperties jwtProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUpAuth(){
        EmpVO emp = new EmpVO();
        emp.setId(99);
        Authentication auth =  new UsernamePasswordAuthenticationToken(emp, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
   }

    @AfterEach
    void cleanAuth(){
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("[Unit] EmpController.createEmp() - 建立重複帳號，應回傳 409 及指定訊息")
    void testCreateEmpUsernameDuplicate() throws Exception {

        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(SeedUserData.TESTER);

        doThrow(new AlreadyExistsException(MessageEnum.USERNAME_ALREADY_EXISTS))
                .when(empService)
                .create(any(EmpCreateDTO.class));

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        ResultActions resultActions = mockMvc.perform(
                        post("/emps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                );

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.USERNAME_ALREADY_EXISTS.getCode()));
    }

    @Test
    @DisplayName("[Unit] EmpController.createEmp() - 建立員工成功，應回傳 200")
    void testCreateEmpSuccess() throws Exception {

        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(SeedUserData.TESTER);

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/emps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));
    }

    @Test
    @DisplayName("[Unit] EmpController.updateEmpStatus() - 變更員工啟用狀態，帳號不存在應回傳 401 及指定訊息")
    void testUpdateEmpStatusAccountNotExist() throws Exception {

        Integer id = 666;
        EmpStatusDTO empStatusDTO = EmpTestDataFactory.getEmpStatusDTO(StatusEnum.ACTIVE);

        doThrow(new NotExistException(MessageEnum.ACCOUNT_NOT_EXISTS))
                .when(empService)
                .updateStatus(any(EmpStatusDTO.class), anyInt()
                );

        String jsonBody = objectMapper.writeValueAsString(empStatusDTO);
        ResultActions resultActions = mockMvc.perform(
                            patch("/emps/{id}/status", id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    );
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXISTS.getCode()));
    }

    @Test
    @DisplayName("[Unit] EmpController.updateEmpStatus() - 變更員工啟用狀態成功，應回傳 200")
    void testUpdateEmpStatusSuccess() throws Exception {

        Integer id = 666;
        EmpStatusDTO empStatusDTO = EmpTestDataFactory.getEmpStatusDTO(StatusEnum.INACTIVE);

        String jsonBody = objectMapper.writeValueAsString(empStatusDTO);
        ResultActions resultActions = mockMvc.perform(
                        patch("/emps/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                );
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));

        verify(empService).updateStatus(any(EmpStatusDTO.class), eq(id));

    }

    @Test
    @DisplayName("[Unit] EmpController.getEmpById() - 員工 id 不存在，應回傳 404")
    void testGetEmpByIdNoExist() throws Exception {
        Integer id = 666;

        doThrow(new NotExistException(MessageEnum.ACCOUNT_NOT_EXISTS))
                .when(empService)
                .getById(anyInt());

        ResultActions resultActions = mockMvc.perform(
                get("/emps/{id}", id)
        );

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXISTS.getCode()));

        verify(empService).getById(anyInt());
    }

    @Test
    @DisplayName("[Unit] EmpController.getEmpById() - 查詢成功應回傳 200 及員工資料")
    void testGetEmpByIdSuccess() throws Exception {
        Integer id = 666;

        EmpVO empVO = new EmpVO();
        BeanUtils.copyProperties(SeedUserData.MANAGER,empVO);
        when(empService.getById(anyInt())).thenReturn(empVO);

        ResultActions resultActions = mockMvc.perform(
                get("/emps/{id}", id)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(empService).getById(anyInt());
    }

    @Test
    @DisplayName("[Unit] EmpController.updateEmpById() - 員工 id 不存在，應回傳 404")
    void testUpdateEmpByIdNoExist() throws Exception {

        EmpEditDTO empEditDTO = EmpTestDataFactory.getEmpEditDTO(SeedUserData.TESTER);
        empEditDTO.setId(Integer.MAX_VALUE);

        doThrow(new NotExistException(MessageEnum.ACCOUNT_NOT_EXISTS))
                .when(empService)
                .updateById(any(EmpEditDTO.class)
                );

        String jsonBody = objectMapper.writeValueAsString(empEditDTO);
        ResultActions resultActions = mockMvc.perform(
                put("/emps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXISTS.getCode()));

        verify(empService).updateById(any(EmpEditDTO.class));
    }



    @Test
    @DisplayName("[Unit] EmpController.updateEmpById() - 修改成功應回傳 200")
    void testUpdateEmpByIdSuccess() throws Exception {

        EmpEditDTO empEditDTO = EmpTestDataFactory.getEmpEditDTO(SeedUserData.TESTER);

        String jsonBody = objectMapper.writeValueAsString(empEditDTO);
        ResultActions resultActions = mockMvc.perform(
                put("/emps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));

        verify(empService).updateById(any(EmpEditDTO.class));
    }

    @Test
    @DisplayName("[Unit] EmpController.pageQueryEmp() - 分頁查詢成功，應回傳 200 及資料")
    void testPageQueryEmpSuccess() throws Exception {

        EmpPageQueryDTO queryDTO = new EmpPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(5);

        List<EmpVO> mockData = new ArrayList<>();
        mockData.add(EmpTestDataFactory.getEmpVO(SeedUserData.MANAGER));
        mockData.add(EmpTestDataFactory.getEmpVO(SeedUserData.STAFF));

        Long total = (long) mockData.size();
        PageResult mockResult = new PageResult(total, queryDTO.getPage(),
                queryDTO.getPageSize(), mockData);

        when(empService.pageQuery(any(EmpPageQueryDTO.class))).thenReturn(mockResult);

        ResultActions resultActions = mockMvc.perform(
                get("/emps/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("name", SeedUserData.MANAGER.name())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());

        verify(empService).pageQuery(any(EmpPageQueryDTO.class));
    }

}
