package com.qqriceball.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.integration.testData.SeedUser;
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
public class LoginControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("[IT] Login - 登入成功，應回傳 200 及 token")
    void testLoginSuccess() throws Exception {

        EmpLoginDTO empLoginDTO = getEmpLoginDTO(
                SeedUser.TESTER.username(), SeedUser.TESTER.password());

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
                .andExpect(jsonPath("$.data.username").value(SeedUser.TESTER.username()))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("[IT] Login - 登入帳號不存在，應回傳 404 及指定訊息")
    void testLoginAccountNotExist() throws Exception {

        EmpLoginDTO empLoginDTO = getEmpLoginDTO("notExist",
                "userPassword");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.ACCOUNT_NOT_EXIST.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] Login - 登入密碼錯誤，應回傳 401 及指定訊息")
    void testLoginPasswordError() throws Exception {


        EmpLoginDTO empLoginDTO = getEmpLoginDTO(SeedUser.MANAGER.username(), "wrongPassword");

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
    @DisplayName("[IT] Login - 登入帳號已停用，應回傳 403 及指定訊息")
    void testLoginAccountInactive() throws Exception {

        EmpLoginDTO empLoginDTO = getEmpLoginDTO(
                SeedUser.INACTIVE.username(), SeedUser.INACTIVE.password());

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
    @DisplayName("[IT] Login - 登入未輸入帳號，應回傳 400")
    void testLoginAccountNull() throws Exception {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setPassword("password");

        String jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.msg").isNotEmpty());
    }


    @Test
    @DisplayName("[IT] Logout - 登出成功，應回傳 200")
    void testLogoutSuccess() throws Exception {

        ResultActions resultActions = mockMvc.perform(
                post("/logout"));

        resultActions.andExpect(status().isOk());

    }


    private static EmpLoginDTO getEmpLoginDTO(String username, String password) {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        return empLoginDTO;
    }
}