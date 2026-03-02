package com.qqriceball.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.integration.testData.emp.SeedUserData;
import com.qqriceball.integration.utils.Utils;
import com.qqriceball.model.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmpControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenManager;
    private String tokenStaff;

    @BeforeAll
    void setUp() throws Exception {

        // 取得 Admin Token
        EmpLoginDTO managerLoginDTO = getEmpLoginDTO(
                SeedUserData.MANAGER.username(), SeedUserData.MANAGER.password());

        String jsonBody = objectMapper.writeValueAsString(managerLoginDTO);
        MvcResult managerResult = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        tokenManager = JsonPath.read(managerResult.getResponse().getContentAsString(),"$.data.token");

        // 取得 Staff Token
        EmpLoginDTO staffLoginDTO = getEmpLoginDTO(
                SeedUserData.STAFF.username(), SeedUserData.STAFF.password());

        jsonBody = objectMapper.writeValueAsString(staffLoginDTO);
        MvcResult staffResult = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        tokenStaff = JsonPath.read(staffResult.getResponse().getContentAsString(),"$.data.token");

        assertAll(
                () -> assertFalse(tokenManager.isBlank()),
                () -> assertFalse(tokenStaff.isBlank())
        );

    }

    @Test
    @DisplayName("[IT] 2001 createEmp - 建立重複帳號，應回傳 409 及指定訊息")
    void testCreateEmpUsernameDuplicate() throws Exception{

        String username = Utils.getUnique("duplicate");

        EmpCreateDTO empCreateDTO = getEmpCreateDTO(username,
                username, RoleEnum.STAFF.getCode());

        // 建立帳號
        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        // 再次建立相同帳號，應無法建立
        mockMvc.perform(
                post("/emps")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.USERNAME_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.USERNAME_ALREADY_EXISTS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty()
                );

    }

    @Test
    @DisplayName("[IT] Emp 2001 - 帳號長度不足，應回傳 400")
    void testCreateEmpUsernameLengthError() throws Exception{

        EmpCreateDTO empCreateDTO = getEmpCreateDTO("u", "testPassword1", RoleEnum.STAFF.getCode());

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.msg").isNotEmpty());

    }


    @Test
    @DisplayName("[IT] 2001 createEmp - 建立帳號成功，應回傳 200 且可使用新帳號進行登入")
    void testCreateEmpUsernameSuccess() throws Exception{

        String username = Utils.getUnique("create");
        EmpCreateDTO empCreateDTO = getEmpCreateDTO(username, username, RoleEnum.MANAGER.getCode());

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/emps")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.name").value(username));

        EmpLoginDTO empLoginDTO = getEmpLoginDTO(empCreateDTO.getUsername(),
                empCreateDTO.getPassword());


        jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(empLoginDTO.getUsername()))
                .andExpect(jsonPath("$.data.token").isNotEmpty());

    }


    @Test
    @DisplayName("[IT] 2001 createEmp - 登入帳號無管理權限，應回傳 403 無法建立帳號 ")
    void testCreateEmpWithoutAdmin() throws Exception{

        String username = Utils.getUnique("create");
        EmpCreateDTO empCreateDTO = getEmpCreateDTO(username, username, RoleEnum.STAFF.getCode());

        String jsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                        .header("Authorization", "Bearer " + tokenStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isForbidden());

        EmpLoginDTO empLoginDTO = getEmpLoginDTO(empCreateDTO.getUsername(),
                empCreateDTO.getPassword());

        jsonBody = objectMapper.writeValueAsString(empLoginDTO);
        ResultActions resultActions = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isNotFound())
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
        ResultActions resultActions = mockMvc.perform(
                patch("/emps/{id}/status",id)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.ACCOUNT_NOT_EXISTS.getCode()));
    }

    @Test
    @DisplayName("[IT] 2003 updateStatus - 登入帳號無管理權限，應回傳 403 且無法變更啟用狀態 ")
    void testUpdateStatusWithoutAdmin() throws Exception {

        // 查詢執行前的帳號狀態
        int id = SeedUserData.STAFF.id();
        ResultActions beforeActionResult = mockMvc.perform(
                get("/emps/{id}", id)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        MvcResult beforeResult = beforeActionResult
                .andExpect(status().isOk())
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
                        .header("Authorization", "Bearer " + tokenStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isForbidden());

        // 查詢執行後的帳號狀態，確認與執行前相同
       mockMvc.perform(
                get("/emps/{id}", id)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
               .andExpect(jsonPath("$.data.status").value(beforeStatus));

    }


    @Test
    @DisplayName("[IT] 2003 updateStatus - 變更啟用狀態成功，應回傳 200 且變更指定狀態 ")
    void testUpdateStatusSuccess() throws Exception {

        // 建立測試帳號,取得 id

        String statusName = Utils.getUnique("status");
        EmpCreateDTO empCreateDTO = getEmpCreateDTO(statusName, statusName, RoleEnum.MANAGER.getCode());

        String createJsonBody = objectMapper.writeValueAsString(empCreateDTO);
        mockMvc.perform(
                post("/emps")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJsonBody))
                .andExpect(status().isOk());

        EmpLoginDTO empLoginDTO = getEmpLoginDTO(empCreateDTO.getUsername(),
                empCreateDTO.getPassword());

        String loginJsonBody = objectMapper.writeValueAsString(empLoginDTO);
        MvcResult result = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(empCreateDTO.getUsername()))
                .andExpect(jsonPath("$.data.name").value(empCreateDTO.getName())).andReturn();

        int id = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
        String tokenTestUser = JsonPath.read(result.getResponse().getContentAsString(),"$.data.token");

        // 變更狀態為停用
        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(StatusEnum.INACTIVE.getCode());

        String inactiveJsonBody = objectMapper.writeValueAsString(empStatusDTO);
        mockMvc.perform(
                patch("/emps/{id}/status",id)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inactiveJsonBody)
        ).andExpect(status().isOk());

        // 查詢執行後的帳號狀態為停用
        mockMvc.perform(
                get("/emps/{id}", id)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(empStatusDTO.getStatus()));

        // 確認已停用帳號 token 無法使用
        mockMvc.perform(
                        get("/emps/{id}", id)
                                .header("Authorization", "Bearer " + tokenTestUser)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden()
        );

        // 變更狀態為啟用
        empStatusDTO.setStatus(StatusEnum.ACTIVE.getCode());

        String activeJsonBody = objectMapper.writeValueAsString(empStatusDTO);
        mockMvc.perform(
                patch("/emps/{id}/status",id)
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activeJsonBody)
        ).andExpect(status().isOk());

        // 查詢執行後的帳號狀態為啟用
        mockMvc.perform(
                        get("/emps/{id}", id)
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(empStatusDTO.getStatus())
                );

    }

    @Test
    @DisplayName("[IT] 2004 getById - 查詢 id 不存在，應回傳 404")
    void testGetByIdNoExist() throws Exception {

        mockMvc.perform(
                        get("/emps/{id}", Integer.MAX_VALUE)
                                .header("Authorization", "Bearer " + tokenManager)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 2004 getById - 查詢成功，應回傳 200 及查詢結果")
    void testGetByIdSuccess() throws Exception {

        mockMvc.perform(
                        get("/emps/{id}", SeedUserData.TESTER.id())
                        .header("Authorization", "Bearer " + tokenManager)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(SeedUserData.TESTER.id()))
                .andExpect(jsonPath("$.data.username").value(SeedUserData.TESTER.username())
                );

    }

    @Test
    @DisplayName("[IT] 2005 updateById - 修改 id 不存在，應回傳 404")
    void testUpdateByIdNoExist() throws Exception {

        EmpEditDTO empEditDTO = new EmpEditDTO();
        empEditDTO.setId(Integer.MAX_VALUE);
        empEditDTO.setEntryDate(LocalDate.now());

        String jsonBody = objectMapper.writeValueAsString(empEditDTO);
        mockMvc.perform(
                        put("/emps")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").isEmpty()
                );
    }

    @Test
    @DisplayName("[IT] 2005 updateById - 修改成功，應回傳 200")
    void testUpdateByIdSuccess() throws Exception {

        EmpEditDTO empEditDTO = new EmpEditDTO();
        empEditDTO.setId(SeedUserData.TESTER.id());
        empEditDTO.setEntryDate(LocalDate.now());

        String jsonBody = objectMapper.writeValueAsString(empEditDTO);
        mockMvc.perform(
                        put("/emps")
                                .header("Authorization", "Bearer " + tokenManager)
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

        ResultActions resultActions = mockMvc.perform(
                get("/emps/page")
                        .header("Authorization", "Bearer " + tokenManager)
                        .param("page", queryDTO.getPage().toString())
                        .param("pageSize", queryDTO.getPageSize().toString())
                        .param("name", queryDTO.getName())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(queryDTO.getPage()))
                .andExpect(jsonPath("$.data.pageSize").value(queryDTO.getPageSize()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isNotEmpty())
                .andExpect(jsonPath("$.data.records[0].id").value(SeedUserData.TESTER.id()))
                .andExpect(jsonPath("$.data.records[0].username").value(SeedUserData.TESTER.username()));
    }


    private static EmpCreateDTO getEmpCreateDTO(String username, String password, int role) {
        EmpCreateDTO empCreateDTO = new EmpCreateDTO();
        empCreateDTO.setUsername(username);
        empCreateDTO.setPassword(password);
        empCreateDTO.setName(username);
        empCreateDTO.setRole(role);
        empCreateDTO.setEntryDate(LocalDate.now());

        return empCreateDTO;
    }

    private static EmpLoginDTO getEmpLoginDTO(String username, String password) {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        return empLoginDTO;
    }
}


