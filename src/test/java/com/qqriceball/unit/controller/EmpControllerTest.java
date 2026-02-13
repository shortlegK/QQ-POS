package com.qqriceball.unit.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.model.dto.EmpCreateDTO;
import com.qqriceball.model.dto.EmpStatusDTO;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.controller.EmpController;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.service.EmpService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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


import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("[Unit] EmpController.createEmp - 建立重複帳號，應回傳 409 及指定訊息")
    void testCreateEmpUsernameDuplicate() throws Exception {

        EmpCreateDTO empCreateDTO = getEmpCreateDTO();

        doThrow(new AlreadyExistsException(MessageEnum.USERNAME_ALREADY_EXIST))
                .when(empService)
                .create(any(EmpCreateDTO.class)
                );

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        ResultActions resultActions = mockMvc.perform(
                        post("/emp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                );

        resultActions
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.USERNAME_ALREADY_EXIST.getCode()));
    }

    @Test
    @DisplayName("[Unit] EmpController.createEmp - 建立員工成功，應回傳 200")
    void testCreateEmpUsernameSuccess() throws Exception {

        EmpCreateDTO empCreateDTO = getEmpCreateDTO();

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/emp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));
    }


    @Test
    @DisplayName("[Unit] EmpController.updateStatus - 變更員工啟用狀態，帳號不存在應回傳 401 及指定訊息")
    void testUpdateStatusAccountNotExist() throws Exception {

        Integer id = 666;
        EmpStatusDTO empStatusDTO = getEmpStatusDTO(StatusEnum.ACTIVE);

        doThrow(new AccountNotExistException(MessageEnum.ACCOUNT_NOT_EXIST))
                .when(empService)
                .updateStatus(any(EmpStatusDTO.class), anyInt()
                );

        String jsonBody = objectMapper.writeValueAsString(empStatusDTO);
        ResultActions resultActions = mockMvc.perform(
                            patch("/emp/{id}/status", id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    );
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXIST.getCode()));


    }



    @Test
    @DisplayName("[Unit] EmpController.updateStatus - 變更員工啟用狀態成功，應回傳 200")
    void testUpdateStatusSuccess() throws Exception {

        Integer id = 666;
        EmpStatusDTO empStatusDTO = getEmpStatusDTO(StatusEnum.ACTIVE);

        String jsonBody = objectMapper.writeValueAsString(empStatusDTO);
        ResultActions resultActions = mockMvc.perform(
                        patch("/emp/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                );
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));

        verify(empService).updateStatus(any(EmpStatusDTO.class), eq(id));

    }

    private EmpCreateDTO getEmpCreateDTO() {

        EmpCreateDTO empCreateDTO = new EmpCreateDTO();
        empCreateDTO.setUsername("tester");
        empCreateDTO.setPassword("Password");
        empCreateDTO.setName("tester");
        empCreateDTO.setRole(RoleEnum.STAFF.getCode());
        empCreateDTO.setEntryDate(LocalDate.of(2026, 1, 1));

        return empCreateDTO;
    }

    private static EmpStatusDTO getEmpStatusDTO(StatusEnum status) {
        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(status.getCode());
        return empStatusDTO;
    }

}
