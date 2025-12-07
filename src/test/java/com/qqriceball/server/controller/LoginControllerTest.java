package com.qqriceball.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AccountInactiveException;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.PasswordErrorException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.constant.MessageEnum;
import com.qqriceball.constant.StatusEnum;
import com.qqriceball.pojo.dto.EmpLoginDTO;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.server.service.EmpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProperties jwtProperties;

    @MockBean
    private EmpService empService;

    @Autowired
    private ObjectMapper objectMapper;

    private String secretkey = "test-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secret";


    @Test
    @DisplayName("登入成功")
    void testLoginSuccess() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("admin");
        empLoginDTO.setPassword("Password");

        Emp fakeEmp = new Emp();
        fakeEmp.setId(1);
        fakeEmp.setUsername("admin");
        fakeEmp.setPassword("Password");
        fakeEmp.setStatus(StatusEnum.ACTIVE.getValue());

        when(empService.login(any(EmpLoginDTO.class))).thenReturn(fakeEmp);

        when(jwtProperties.getAdminSecretKey()).thenReturn(secretkey);
        when(jwtProperties.getAdminTtl()).thenReturn(3600000L);

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions.andExpect(status().isOk());

        verify(empService).login(argThat(dto ->
                "admin".equals(dto.getUsername())
        ));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("登入失敗，帳號不存在")
    void testLoginAccountNotExist() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("NotExist");
        empLoginDTO.setPassword("Password");

        when(empService.login(argThat(dto -> "NotExist".equals(empLoginDTO.getUsername()))))
                .thenThrow(new AccountNotExistException(MessageEnum.ACCOUNT_NOT_EXIST));

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(1002));

    }

    @Test
    @DisplayName("登入失敗，密碼錯誤")
    void testLoginPasswordError() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("wrongUser");
        empLoginDTO.setPassword("wrongPassword");


        when(empService.login(argThat(dto -> "wrongUser".equals(empLoginDTO.getUsername()))))
                .thenThrow(new PasswordErrorException(MessageEnum.PASSWORD_ERROR));

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(1001));


    }

    @Test
    @DisplayName("登入失敗，帳號停用")
    void testLoginAccountInactive() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("inactiveUser");
        empLoginDTO.setPassword("wrongPassword");


        when(empService.login(argThat(dto -> "inactiveUser".equals(empLoginDTO.getUsername()))))
                .thenThrow(new AccountInactiveException(MessageEnum.ACCOUNT_INACTIVE));

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions.andExpect(status().isForbidden());
        resultActions.andExpect(jsonPath("$.code").value(1003));


    }


    @Test
    @DisplayName("登出帳號成功")
    void logout() throws  Exception {

        ResultActions resultActions = mockMvc.perform(
                post("/logout")
        );

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.code").value(200));

    }
}