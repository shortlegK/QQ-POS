package com.qqriceball.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.constant.MessageConstant;
import com.qqriceball.pojo.dto.EmpLoginDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("[Integration] Login - 登入成功，應回傳 200 及 token")
    void testLoginSuccess() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("admin");
        empLoginDTO.setPassword("123456");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("[Integration] Login - 登入帳號不存在，應回傳 401 及指定訊息")
    void testLoginAccountNotExist() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("NoExist");
        empLoginDTO.setPassword("123456");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageConstant.ACCOUNT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageConstant.ACCOUNT_NOT_EXIST.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[Integration] Login - 登入密碼錯誤，應回傳 401 及指定訊息")
    void testLoginPasswordError() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("admin");
        empLoginDTO.setPassword("123555");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageConstant.PASSWORD_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageConstant.PASSWORD_ERROR.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("[Integration] Login - 登入帳號已停用，應回傳 403 及指定訊息")
    void testLoginAccountInactive() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("inactiveUser");
        empLoginDTO.setPassword("123456");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(MessageConstant.ACCOUNT_INACTIVE.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageConstant.ACCOUNT_INACTIVE.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[Integration] Logout - 登出成功，應回傳 200")
    void testLogoutSuccess() throws Exception {

        ResultActions resultActions = mockMvc.perform(
                post("/logout"));

        resultActions.andExpect(status().isOk());

    }
}