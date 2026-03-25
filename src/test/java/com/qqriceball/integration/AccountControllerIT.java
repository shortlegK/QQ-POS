package com.qqriceball.integration;

import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.model.dto.emp.EmpLoginDTO;
import com.qqriceball.model.dto.emp.EmpUpdatePasswordDTO;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.utils.emp.EmpTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

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

        mockMvc.perform(
                patch("/accounts/password")
                        .header("Authorization", "Bearer " + tokenStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString());

        // 使用新密碼登入
        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.STAFF.username(), SeedUserData.STAFF.password()+"new");
        String loginJsonBody = objectMapper.writeValueAsString(empLoginDTO);
        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString());
    }

    @Test
    @DisplayName("[IT] AccountController.updatePassword() - 帳號為管理權限，成功更新密碼應回傳 200")
    void testUpdatePasswordSuccessManager() throws Exception{
        EmpUpdatePasswordDTO updatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO(SeedUserData.MANAGER.password()
                ,SeedUserData.MANAGER.password()+"new");

        String jsonBody = objectMapper.writeValueAsString(updatePasswordDTO);

        mockMvc.perform(
                        patch("/accounts/password")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString());

        // 使用新密碼登入
        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.MANAGER.username(), SeedUserData.MANAGER.password()+"new");
        String loginJsonBody = objectMapper.writeValueAsString(empLoginDTO);
        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString());
    }

    @Test
    @DisplayName("[IT] AccountController.updatePassword() - 舊密碼錯誤應回傳 400")
    void testUpdatePasswordOldPasswordError() throws Exception{
        EmpUpdatePasswordDTO updatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO("wrong-old-password"
                ,SeedUserData.STAFF.password()+"new");

        String jsonBody = objectMapper.writeValueAsString(updatePasswordDTO);

        mockMvc.perform(
                        patch("/accounts/password")
                                .header("Authorization", "Bearer " + tokenStaff)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.OLD_PASSWORD_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OLD_PASSWORD_ERROR.getMessage()));
    }

}
