package com.qqriceball.integration;

import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.model.dto.emp.EmpLoginDTO;
import com.qqriceball.model.dto.emp.EmpUpdatePasswordDTO;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.utils.emp.EmpTestDataFactory;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerIT extends BaseIntegrationTest{

    @Test
    @DisplayName("[IT] AccountController.updatePassword() - 帳號為一般權限，成功更新密碼應回傳 200")
    void testUpdatePasswordSuccessStaff() throws Exception{
        EmpUpdatePasswordDTO updatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO(SeedUserData.STAFF.password()
                ,SeedUserData.STAFF.password()+"new");

        String jsonBody = objectMapper.writeValueAsString(updatePasswordDTO);

       MvcResult result =  mockMvc.perform(
                patch("/accounts/password")
                         .cookie(cookieStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
               .andReturn();

       Cookie cookie = result.getResponse().getCookie("access_token");
       assertNotNull(cookie);
       assertAll(
               () -> assertTrue(cookie.isHttpOnly()),
               () -> assertFalse(cookie.getValue().isBlank())
       );

       // 使用新密碼登入
        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.STAFF.username(), SeedUserData.STAFF.password()+"new");
        String loginJsonBody = objectMapper.writeValueAsString(empLoginDTO);
        MvcResult result2 = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        Cookie cookie2 = result2.getResponse().getCookie("access_token");
        assertNotNull(cookie2);
        assertAll(
                () -> assertTrue(cookie2.isHttpOnly()),
                () -> assertFalse(cookie2.getValue().isBlank())
        );
    }

    @Test
    @DisplayName("[IT] AccountController.updatePassword() - 帳號為管理權限，成功更新密碼應回傳 200")
    void testUpdatePasswordSuccessManager() throws Exception{
        EmpUpdatePasswordDTO updatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO(SeedUserData.MANAGER.password()
                ,SeedUserData.MANAGER.password()+"new");

        String jsonBody = objectMapper.writeValueAsString(updatePasswordDTO);

        MvcResult result = mockMvc.perform(
                        patch("/accounts/password")
                                 .cookie(cookieManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("access_token");
        assertNotNull(cookie);
        assertAll(
                () -> assertTrue(cookie.isHttpOnly()),
                () -> assertFalse(cookie.getValue().isBlank())
        );

        // 使用新密碼登入
        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.MANAGER.username(), SeedUserData.MANAGER.password()+"new");
        String loginJsonBody = objectMapper.writeValueAsString(empLoginDTO);
        MvcResult result2 = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        Cookie cookie2 = result2.getResponse().getCookie("access_token");
        assertNotNull(cookie2);
        assertAll(
                () -> assertTrue(cookie2.isHttpOnly()),
                () -> assertFalse(cookie2.getValue().isBlank())
        );
    }

    @Test
    @DisplayName("[IT] AccountController.updatePassword() - 舊密碼錯誤應回傳 400")
    void testUpdatePasswordOldPasswordError() throws Exception{
        EmpUpdatePasswordDTO updatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO("wrong-old-password"
                ,SeedUserData.STAFF.password()+"new");

        String jsonBody = objectMapper.writeValueAsString(updatePasswordDTO);

        mockMvc.perform(
                        patch("/accounts/password")
                                 .cookie(cookieStaff)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.OLD_PASSWORD_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OLD_PASSWORD_ERROR.getMessage()));
    }

}
