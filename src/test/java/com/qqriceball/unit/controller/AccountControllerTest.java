package com.qqriceball.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.common.utils.CookieHelper;
import com.qqriceball.controller.AccountController;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.model.dto.emp.EmpUpdatePasswordDTO;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.service.EmpService;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.utils.TestDataGenerator;
import com.qqriceball.utils.emp.EmpTestDataFactory;
import jakarta.servlet.http.Cookie;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({GlobalExceptionHandler.class,CookieHelper.class})
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

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
        emp.setUsername(SeedUserData.TESTER.username());
        Authentication auth =  new UsernamePasswordAuthenticationToken(emp, null, java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void cleanAuth(){
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("[Unit] AccountController.updatePassword() - 更新密碼成功應回傳 200 及新 token")
    void testUpdatePasswordSuccess() throws Exception {
        String secretKey = "test-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secrettest-secret";

        EmpUpdatePasswordDTO empUpdatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO(SeedUserData.TESTER.password()
                , TestDataGenerator.getUnique(SeedUserData.TESTER.password()));

        when(jwtProperties.getSecretKey()).thenReturn(secretKey);
        when(jwtProperties.getTtlMillis()).thenReturn(3600000L);
        when(jwtProperties.getCookieName()).thenReturn("access_token");
        when(jwtProperties.isCookieSecure()).thenReturn(false);
        when(jwtProperties.getCookieSameSite()).thenReturn("Strict");

        String jsonBody = objectMapper.writeValueAsString(empUpdatePasswordDTO);
        MvcResult result = mockMvc.perform(
                patch("/accounts/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
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
    @DisplayName("[Unit] AccountController.updatePassword() - 舊密碼錯誤應回傳 400")
    void testUpdatePasswordOldPasswordError() throws Exception {

        EmpUpdatePasswordDTO empUpdatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO("wrong-old-password"
                , SeedUserData.TESTER.password()+"new");
        doThrow(new BadRequestArgsException(MessageEnum.OLD_PASSWORD_ERROR))
                .when(empService).updatePassword(anyString(), any(EmpUpdatePasswordDTO.class));

        String jsonBody = objectMapper.writeValueAsString(empUpdatePasswordDTO);
        mockMvc.perform(
                patch("/accounts/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isBadRequest())
         .andExpect(jsonPath("$.code").value(MessageEnum.OLD_PASSWORD_ERROR.getCode()))
         .andExpect(jsonPath("$.msg").value(MessageEnum.OLD_PASSWORD_ERROR.getMessage()));
    }

    @Test
    @DisplayName("[Unit] AccountController.updatePassword() - 缺少必要參數應回傳 400")
    void testUpdatePasswordMissingParameters() throws Exception {
        EmpUpdatePasswordDTO empUpdatePasswordDTO = new EmpUpdatePasswordDTO();

        String jsonBody = objectMapper.writeValueAsString(empUpdatePasswordDTO);
        mockMvc.perform(
                patch("/accounts/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.BAD_REQUEST.getCode()));
    }
}
