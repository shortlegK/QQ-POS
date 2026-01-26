package com.qqriceball.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.enumeration.MessageEnum;
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

        String username = "tester";
        String password = "(Qqpos1357";
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("[Integration] Login - 登入帳號不存在，應回傳 401 及指定訊息")
    void testLoginAccountNotExist() throws Exception {

        String username = "NoExist";
        String password = "123456";
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.ACCOUNT_NOT_EXIST.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[Integration] Login - 登入密碼錯誤，應回傳 401 及指定訊息")
    void testLoginPasswordError() throws Exception {

        String username = "admin";
        String password = "wrongPassword";
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageEnum.PASSWORD_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.PASSWORD_ERROR.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("[Integration] Login - 登入帳號已停用，應回傳 403 及指定訊息")
    void testLoginAccountInactive() throws Exception {

        String username = "inactive";
        String password = "(Qqpos1357";
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_INACTIVE.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.ACCOUNT_INACTIVE.getMessage()))
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