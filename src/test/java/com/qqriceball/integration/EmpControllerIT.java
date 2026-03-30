package com.qqriceball.integration;

import com.jayway.jsonpath.JsonPath;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.model.dto.emp.*;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.utils.TestDataGenerator;
import com.qqriceball.utils.emp.EmpTestDataFactory;
import jakarta.servlet.http.Cookie;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.*;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmpControllerIT extends BaseIntegrationTest{

    @Test
    @DisplayName("[IT] 2001 createEmp - 建立重複帳號，應回傳 409 及指定訊息")
    void testCreateEmpUsernameDuplicate() throws Exception{

        String username = TestDataGenerator.getUnique("duplicate");
        String password = "testPassword1";

        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(username,
                password, RoleEnum.STAFF.getCode());

        // 建立帳號
        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        // 再次建立相同帳號，應無法建立
        mockMvc.perform(
                post("/emps")
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.USERNAME_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.USERNAME_ALREADY_EXISTS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty()
                );

    }

    @Test
    @DisplayName("[IT] 2001 createEmp - 帳號長度不足，應回傳 400")
    void testCreateEmpUsernameLengthError() throws Exception{

        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO("u", "testPassword1", RoleEnum.STAFF.getCode());

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.msg").isNotEmpty());

    }

    @Test
    @DisplayName("[IT] 2001 createEmp - 密碼格式不符規範，應回傳 400")
    void testCreateEmpPasswordFormatError() throws Exception{
        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO("u", "testpassword1", RoleEnum.STAFF.getCode());

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                        post("/emps")
                                 .cookie(cookieManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isBadRequest());

    }


    @Test
    @DisplayName("[IT] 2001 createEmp - 建立帳號成功，應回傳 200 且可使用新帳號進行登入")
    void testCreateEmpUsernameSuccess() throws Exception{

        String username = TestDataGenerator.getUnique("create");
        String password = "testPassword1";
        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(username, password, RoleEnum.MANAGER.getCode());

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.name").value(username));

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(empCreateDTO.getUsername(),
                empCreateDTO.getPassword());


        jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        MvcResult result = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(empLoginDTO.getUsername()))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("access_token");
        assertAll(
                () -> assertNotNull(cookie),
                () -> assertTrue(cookie.isHttpOnly()),
                () -> assertFalse(cookie.getValue().isBlank())
        );
    }


    @Test
    @DisplayName("[IT] 2001 createEmp - 帳號無管理權限嘗試建立員工資料，應回傳 403 無法建立帳號 ")
    void testCreateEmpWithoutAdmin() throws Exception{

        String username = TestDataGenerator.getUnique("create");
        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(username, username, RoleEnum.STAFF.getCode());

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                         .cookie(cookieStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isForbidden());

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(empCreateDTO.getUsername(),
                empCreateDTO.getPassword());

        jsonBody = objectMapper.writeValueAsString(empLoginDTO);
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
    @DisplayName("[IT] 2003 updateStatus - 員工 id 不存在，變更啟用狀態應回傳 404")
    void testUpdateStatusAccountNotExist() throws Exception{

        int id = Integer.MAX_VALUE;
        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(StatusEnum.INACTIVE.getCode());

        String jsonBody = objectMapper.writeValueAsString(empStatusDTO);
        mockMvc.perform(
                patch("/emps/{id}/status",id)
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXISTS.getCode()));
    }

    @Test
    @DisplayName("[IT] 2003 updateStatus - 登入帳號無管理權限，應回傳 403 且無法變更啟用狀態 ")
    void testUpdateStatusWithoutAdmin() throws Exception {

        // 查詢執行前的帳號狀態
        int id = SeedUserData.STAFF.id();
        MvcResult beforeResult  = mockMvc.perform(
                get("/emps/{id}", id)
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").isNotEmpty())
                .andReturn();

        String beforeResponseBody = beforeResult.getResponse().getContentAsString();
        int beforeStatus = JsonPath.read(beforeResponseBody, "$.data.status");


        // 嘗試變更啟用狀態為停用
        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(StatusEnum.INACTIVE.getCode());

        String jsonBody = objectMapper.writeValueAsString(empStatusDTO);
        mockMvc.perform(
                patch("/emps/{id}/status",id)
                         .cookie(cookieStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isForbidden());

        // 查詢執行後的帳號狀態，確認與執行前相同
       mockMvc.perform(
                get("/emps/{id}", id)
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
               .andExpect(jsonPath("$.data.status").value(beforeStatus));

    }


    @Test
    @DisplayName("[IT] 2003 updateStatus - 變更啟用狀態成功，應回傳 200 且變更指定狀態 ")
    void testUpdateStatusSuccess() throws Exception {

        // 建立測試帳號,取得 id

        String statusName = TestDataGenerator.getUnique("status");
        String password = "testPassword1";
        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(statusName, password, RoleEnum.MANAGER.getCode());

        String createJsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJsonBody))
                .andExpect(status().isOk());

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(empCreateDTO.getUsername(),
                empCreateDTO.getPassword());

        String loginJsonBody = objectMapper.writeValueAsString(empLoginDTO);
        MvcResult result = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(empCreateDTO.getUsername()))
                .andExpect(jsonPath("$.data.name").value(empCreateDTO.getName()))
                .andReturn();

        int id = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
        Cookie cookieTestUser = result.getResponse().getCookie("access_token");

        // 變更狀態為停用
        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(StatusEnum.INACTIVE.getCode());

        String inactiveJsonBody = objectMapper.writeValueAsString(empStatusDTO);
        mockMvc.perform(
                patch("/emps/{id}/status",id)
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inactiveJsonBody)
        ).andExpect(status().isOk());

        // 查詢執行後的帳號狀態為停用
        mockMvc.perform(
                get("/emps/{id}", id)
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(empStatusDTO.getStatus()));

        // 確認已停用帳號 token 無法使用
        mockMvc.perform(
                        get("/emps/{id}", id)
                                .cookie(cookieTestUser)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden()
        );

        // 變更狀態為啟用
        empStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        String activeJsonBody = objectMapper.writeValueAsString(empStatusDTO);
        mockMvc.perform(
                patch("/emps/{id}/status",id)
                         .cookie(cookieManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activeJsonBody)
        ).andExpect(status().isOk());

        // 查詢執行後的帳號狀態為啟用
        mockMvc.perform(
                        get("/emps/{id}", id)
                                 .cookie(cookieManager)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(empStatusDTO.getStatus())
                );

    }

    @Test
    @DisplayName("[IT] 2004 getEmpById - 查詢 id 不存在，應回傳 404")
    void testGetByIdNoExist() throws Exception {

        mockMvc.perform(
                        get("/emps/{id}", Integer.MAX_VALUE)
                                 .cookie(cookieManager)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 2004 getEmpById - 查詢成功，應回傳 200 及查詢結果")
    void testGetByIdSuccess() throws Exception {

        mockMvc.perform(
                        get("/emps/{id}", SeedUserData.TESTER.id())
                         .cookie(cookieManager)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(SeedUserData.TESTER.id()))
                .andExpect(jsonPath("$.data.username").value(SeedUserData.TESTER.username())
                );

    }

    @Test
    @DisplayName("[IT] 2005 updateEmpById - 修改 id 不存在，應回傳 404")
    void testUpdateByIdNoExist() throws Exception {

        EmpEditDTO empEditDTO = new EmpEditDTO();
        BeanUtils.copyProperties(SeedUserData.TESTER, empEditDTO);
        empEditDTO.setId(Integer.MAX_VALUE);
        empEditDTO.setEntryDate(LocalDate.now());

        String jsonBody = objectMapper.writeValueAsString(empEditDTO);
        mockMvc.perform(
                        put("/emps")
                                 .cookie(cookieManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").isEmpty()
                );
    }

    @Test
    @DisplayName("[IT] 2005 updateEmpById - 修改成功，應回傳 200")
    void testUpdateByIdSuccess() throws Exception {

        EmpEditDTO empEditDTO = new EmpEditDTO();
        BeanUtils.copyProperties(SeedUserData.TESTER, empEditDTO);
        empEditDTO.setEntryDate(LocalDate.now());

        String jsonBody = objectMapper.writeValueAsString(empEditDTO);
        mockMvc.perform(
                        put("/emps")
                                 .cookie(cookieManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(empEditDTO.getId()))
                .andExpect(jsonPath("$.data.entryDate").value(empEditDTO.getEntryDate().toString()));
    }

    @Test
    @DisplayName("[IT] 2002 pageQueryEmp - 分頁查詢成功，應回傳 200 及資料")
    void testPageQueryEmpSuccess() throws Exception {

        EmpPageQueryDTO queryDTO = new EmpPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(5);
        queryDTO.setName(SeedUserData.TESTER.name());

        mockMvc.perform(
                get("/emps/page")
                         .cookie(cookieManager)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("name", queryDTO.getName())
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(queryDTO.getPage()))
                .andExpect(jsonPath("$.data.pageSize").value(queryDTO.getPageSize()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].id").value(SeedUserData.TESTER.id()))
                .andExpect(jsonPath("$.data.records[0].username").value(SeedUserData.TESTER.username()));
    }

    @Test
    @DisplayName("[IT] 2002 pageQueryEmp - 分頁查詢指定狀態，應回傳 200 及資料")
    void testPageQueryEmpByStatus() throws Exception {

        EmpPageQueryDTO queryDTO = new EmpPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(5);
        queryDTO.setStatus(StatusEnum.INACTIVE.getCode());

        mockMvc.perform(
                get("/emps/page")
                         .cookie(cookieManager)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("status", queryDTO.getStatus().toString())
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(queryDTO.getPage()))
                .andExpect(jsonPath("$.data.pageSize").value(queryDTO.getPageSize()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[*].status").value(everyItem(equalTo(queryDTO.getStatus()))));
    }

    @Test
    @DisplayName("[IT] 2002 pageQueryEmp - 分頁查詢指定名稱及狀態，應回傳 200 及資料")
    void testPageQueryEmpByNameAndStatus() throws Exception {

        String keyword = SeedUserData.TESTER.name().substring(1, 3);

        EmpPageQueryDTO queryDTO = new EmpPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(5);
        queryDTO.setName(keyword);
        queryDTO.setStatus(StatusEnum.ACTIVE.getCode());

        mockMvc.perform(
                get("/emps/page")
                         .cookie(cookieManager)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("name", queryDTO.getName())
                        .param("status", queryDTO.getStatus().toString())
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(queryDTO.getPage()))
                .andExpect(jsonPath("$.data.pageSize").value(queryDTO.getPageSize()))
                .andExpect(jsonPath("$.data.records").isNotEmpty())
                .andExpect(jsonPath("$.data.records[*].name").value(everyItem(containsString(keyword))))
                .andExpect(jsonPath("$.data.records[*].status").value(everyItem(equalTo(queryDTO.getStatus()))));
    }
}


