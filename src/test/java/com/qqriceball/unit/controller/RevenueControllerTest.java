package com.qqriceball.unit.controller;


import com.qqriceball.common.properties.JwtProperties;
import com.qqriceball.controller.RevenueController;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.PeriodTypeEnum;
import com.qqriceball.handler.GlobalExceptionHandler;
import com.qqriceball.model.dto.order.*;
import com.qqriceball.model.dto.revenue.RevenueDTO;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.model.vo.revenue.RevenueStatsVO;
import com.qqriceball.service.EmpService;
import com.qqriceball.service.RevenueService;
import com.qqriceball.utils.revenue.RevenueTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(RevenueController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RevenueService revenueService;

    @MockBean
    private JwtProperties jwtProperties;

    @MockBean
    EmpService empService;

    @BeforeEach
    void setUpAuth() {
        EmpVO emp = new EmpVO();
        emp.setId(99);
        Authentication auth = new UsernamePasswordAuthenticationToken(emp, null, emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void cleanAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("[Unit] RevenueController.getRevenueStatsByPeriodType() - 查詢營收統計資料成功，應回傳 200 及資料")
    void testGetRevenueStatsByPeriodTypeSuccess() throws Exception {

        RevenueStatsVO revenueStatsVO = RevenueTestDataFactory.getRevenueStatsVO();
        when(revenueService.getByPeriodType(any(RevenueDTO.class))).thenReturn(revenueStatsVO);

        mockMvc.perform(
                        get("/revenue")
                                .param("periodType", PeriodTypeEnum.TODAY.getCode() + "")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").exists());
        verify(revenueService).getByPeriodType(any(RevenueDTO.class));
    }

    @Test
    @DisplayName("[Unit] RevenueController.getRevenueStatsByPeriodType() - 查詢營收統計資料，狀態參數超出範圍應回傳 400")
    void testGetRevenueStatsByPeriodTypeInvalidStatus() throws Exception {

        mockMvc.perform(
                        get("/revenue")
                                .param("periodType", PeriodTypeEnum.values().length + "")
                ).andExpect(status().isBadRequest());

        verify(revenueService,never()).getByPeriodType(any(RevenueDTO.class));
    }

}