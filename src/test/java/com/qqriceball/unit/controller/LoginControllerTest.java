package com.qqriceball.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.AccountInactiveException;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.PasswordErrorException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.model.dto.EmpLoginDTO;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.controller.LoginController;
import com.qqriceball.service.EmpService;
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

    @Test
    @DisplayName("[Unit] LoginController.login - 登入帳號成功應回傳 200 及 token")
    void testLoginSuccess() throws Exception {

        String secretKey = "test-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secret";

        String username = "manager";
        String password = "Password";

        EmpLoginDTO empLoginDTO = getEmpLoginDTO(username, password);

        Emp fakeEmp = new Emp();
        fakeEmp.setId(1);
        fakeEmp.setUsername(username);
        fakeEmp.setPassword(password);
        fakeEmp.setStatus(StatusEnum.ACTIVE.getCode());

        when(empService.login(any(EmpLoginDTO.class))).thenReturn(fakeEmp);

        when(jwtProperties.getSecretKey()).thenReturn(secretKey);
        when(jwtProperties.getTtlMillis()).thenReturn(3600000L);

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions.andExpect(status().isOk());

        verify(empService).login(argThat(dto ->
                username.equals(dto.getUsername())
        ));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("[Unit] LoginController.login - 登入失敗，帳號不存在應回傳 404 及指定訊息")
    void testLoginAccountNotExist() throws Exception {

        String username = "NotExist";
        String password = "Password";

        EmpLoginDTO empLoginDTO = getEmpLoginDTO(username, password);

        when(empService.login(argThat(dto -> username.equals(dto.getUsername()))))
                .thenThrow(new AccountNotExistException(MessageEnum.ACCOUNT_NOT_EXIST));

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXIST.getCode()));

    }

    @Test
    @DisplayName("[Unit] LoginController.login - 登入失敗，密碼錯誤應回傳 401 及指定訊息")
    void testLoginPasswordError() throws Exception {

        String username = "wrongUser";
        String password = "wrongPassword";
        EmpLoginDTO empLoginDTO = getEmpLoginDTO(username, password);


        when(empService.login(argThat(dto -> username.equals(dto.getUsername()))))
                .thenThrow(new PasswordErrorException(MessageEnum.PASSWORD_ERROR));

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageEnum.PASSWORD_ERROR.getCode()));


    }

    @Test
    @DisplayName("[Unit] LoginController.login - 登入失敗，帳號停用應回傳 403 及指定訊息")
    void testLoginAccountInactive() throws Exception {

        String username = "inactive";
        String password = "wrongPassword";
        EmpLoginDTO empLoginDTO = getEmpLoginDTO(username, password);


        when(empService.login(argThat(dto -> username.equals(dto.getUsername()))))
                .thenThrow(new AccountInactiveException(MessageEnum.ACCOUNT_INACTIVE));

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_INACTIVE.getCode()));


    }


    @Test
    @DisplayName("[Unit] LoginController.logout - 登出帳號成功應回傳 200")
    void logout() throws  Exception {

        ResultActions resultActions = mockMvc.perform(
                post("/logout")
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()));

    }


    private static EmpLoginDTO getEmpLoginDTO(String username, String password) {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        return empLoginDTO;
    }

}