package com.qqriceball.integration;

import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.model.dto.emp.EmpLoginDTO;
import com.qqriceball.utils.emp.EmpTestDataFactory;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginControllerIT extends BaseIntegrationTest{

    @Test
    @DisplayName("[IT] 1001 login - 登入成功，應回傳 200 及 token")
    void testLoginSuccess() throws Exception {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(
                SeedUserData.TESTER.username(), SeedUserData.TESTER.password());

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        MvcResult result = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(SeedUserData.TESTER.username()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("access_token");
        assertNotNull(cookie);
        assertAll(
                () -> assertTrue(cookie.isHttpOnly()),
                () -> assertFalse(cookie.getValue().isBlank())
        );
    }

    @Test
    @DisplayName("[IT] 1001 login - 登入帳號不存在，應回傳 404 及指定訊息")
    void testLoginAccountNotExist() throws Exception {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO("notExist",
                "userPassword");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.ACCOUNT_NOT_EXISTS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 1001 login - 登入密碼錯誤，應回傳 401 及指定訊息")
    void testLoginPasswordError() throws Exception {


        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.MANAGER.username(), "wrongPassword");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageEnum.PASSWORD_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PASSWORD_ERROR.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("[IT] 1001 login - 登入帳號已停用，應回傳 403 及指定訊息")
    void testLoginAccountInactive() throws Exception {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(
                SeedUserData.INACTIVE.username(), SeedUserData.INACTIVE.password());

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_INACTIVE.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.ACCOUNT_INACTIVE.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 1001 login - 登入未輸入帳號，應回傳 400")
    void testLoginAccountNull() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setPassword("password");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.msg").isNotEmpty());
    }


    @Test
    @DisplayName("[IT] 1002 logout - 登出成功，應回傳 200")
    void testLogoutSuccess() throws Exception {

        mockMvc.perform(
                post("/logout"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("[IT] 1003 refreshToken - 刷新 Token 成功，應回傳 200 及新 token")
    void testRefreshTokenSuccess() throws Exception {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(
                SeedUserData.TESTER.username(), SeedUserData.TESTER.password());

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        MvcResult resultLogin = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        Cookie cookieToken = resultLogin.getResponse().getCookie("access_token");

        MvcResult result = mockMvc.perform(
                post("/token/refresh")
                        .cookie(cookieToken)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("access_token");
        assertNotNull(cookie);
        assertAll(
                () -> assertTrue(cookie.isHttpOnly()),
                () -> assertFalse(cookie.getValue().isBlank())
        );
    }

    @Test
    @DisplayName("[IT] 1003 refreshToken - token 無效，應回傳 401 及指定訊息")
    void testRefreshTokenInvalidToken() throws Exception {
        mockMvc.perform(
                post("/token/refresh")
                        .cookie( new Cookie("access_token","invalidToken"))
                ).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageEnum.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.UNAUTHORIZED.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }


}