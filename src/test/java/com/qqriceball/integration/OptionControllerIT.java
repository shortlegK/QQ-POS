package com.qqriceball.integration;


import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.OptionCreateDTO;
import com.qqriceball.model.dto.OptionPageQueryDTO;
import com.qqriceball.service.OptionService;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.utils.TestDataGenerator;
import com.qqriceball.utils.option.OptionTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OptionControllerIT extends BaseIntegrationTest{

    @Autowired
    private OptionMapper optionMapper;
    @Autowired
    private OptionService optionService;

    @Test
    @DisplayName("[IT] 4001 createOption - 建立產品細節選項，應回傳 200 及資料")
    void testCreateOptionSuccess() throws Exception{

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        optionCreateDTO.setTitle(TestDataGenerator.getUnique(SeedOptionData.PURPLE_RICE.title()));

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.title").value(optionCreateDTO.getTitle()))
                .andExpect(jsonPath("$.data.optionType").value(optionCreateDTO.getOptionType()))
                .andExpect(jsonPath("$.data.price").value(optionCreateDTO.getPrice()));
    }

    @Test
    @DisplayName("[IT] 4001 createOption - 建立產品細節選項名稱重複，應回傳 409 及指定訊息")
    void testCreateOptionTitleDuplicate() throws Exception{
        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        optionCreateDTO.setTitle(TestDataGenerator.getUnique(SeedOptionData.PURPLE_RICE.title()));

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk());

        mockMvc.perform(
                post("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_ALREADY_EXISTS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 4001 createOption - 非管理員權限進行新增產品細節選項，應回傳 403")
    void testCreateOptionNoPermission() throws Exception{
        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        optionCreateDTO.setTitle(TestDataGenerator.getUnique(SeedOptionData.PURPLE_RICE.title()));

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .header("Authorization", "Bearer " + tokenStaff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[IT] 4002 pageQueryOption - 分頁查詢成功，應回傳 200 及資料")
    void testPageQueryOptionSuccess() throws Exception{

        OptionPageQueryDTO optionPageQueryDTO = OptionTestDataFactory.getOptionPageQueryDTO(1,5,null, OptionTypeEnum.RICE_SIZE.getCode(), null);

        mockMvc.perform(
                get("/options/page")
                        .header("Authorization", "Bearer " + tokenManager)
                        .param("page", optionPageQueryDTO.getPage().toString())
                        .param("pageSize",optionPageQueryDTO.getPageSize().toString())
                        .param("optionType",String.valueOf(optionPageQueryDTO.getOptionType()))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(optionPageQueryDTO.getPage()))
                .andExpect(jsonPath("$.data.pageSize").value(optionPageQueryDTO.getPageSize()))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].optionType").value(optionPageQueryDTO.getOptionType()));
    }
}
