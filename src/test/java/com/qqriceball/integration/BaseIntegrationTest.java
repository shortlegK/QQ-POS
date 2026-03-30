package com.qqriceball.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqriceball.model.dto.emp.EmpLoginDTO;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.testData.emp.TestAccount;
import com.qqriceball.utils.emp.EmpTestDataFactory;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected Cookie cookieManager;
    protected Cookie cookieStaff;

    @BeforeAll
    void setUp() throws Exception {
        cookieManager = getAuthCookie(SeedUserData.MANAGER);
        cookieStaff = getAuthCookie(SeedUserData.STAFF);

        assertAll(
                () -> assertNotNull(cookieManager),
                () -> assertNotNull(cookieStaff)
        );
    }

    private jakarta.servlet.http.Cookie getAuthCookie(TestAccount account) throws Exception{

        EmpLoginDTO loginDTO = EmpTestDataFactory.getEmpLoginDTO(account.username(), account.password());

        String jsonBody = objectMapper.writeValueAsString(loginDTO);
        MvcResult result = mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("access_token");

    }
}
