package com.qqriceball.integration;


import com.jayway.jsonpath.JsonPath;
import com.qqriceball.enumeration.DefaultEnum;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.option.OptionCreateDTO;
import com.qqriceball.model.dto.option.OptionEditDTO;
import com.qqriceball.model.dto.option.OptionPageQueryDTO;
import com.qqriceball.model.dto.option.OptionStatusDTO;
import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.utils.TestDataGenerator;
import com.qqriceball.utils.option.OptionTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OptionControllerIT extends BaseIntegrationTest{

    @Autowired
    private OptionMapper optionMapper;

    @Test
    @DisplayName("[IT] 4001 createOption - 建立產品細節選項成功，回傳 200 及資料")
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
    @DisplayName("[IT] 4001 createOption - 建立非 AddOn 類型選項並設為預設時,應將該類型原有預設選項調整為非預設，回傳 200 及資料")
    void testCreateOptionDefaultSettingSuccess() throws Exception{

        // 建立第一個預設選項為是的 Option 資料
        OptionCreateDTO firstDefaultOption = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        firstDefaultOption.setTitle(TestDataGenerator.getUnique(SeedOptionData.PURPLE_RICE.title()));
        firstDefaultOption.setIsDefault(DefaultEnum.YES.getCode());

        String jsonBody = objectMapper.writeValueAsString(firstDefaultOption);
        MvcResult firstResult = mockMvc.perform(
                post("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.title").value(firstDefaultOption.getTitle()))
                .andExpect(jsonPath("$.data.optionType").value(firstDefaultOption.getOptionType()))
                .andExpect(jsonPath("$.data.isDefault").value(firstDefaultOption.getIsDefault()))
                .andExpect(jsonPath("$.data.price").value(firstDefaultOption.getPrice()))
                .andReturn();

        Integer firstDefaultOptionId = JsonPath.read(firstResult.getResponse().getContentAsString(), "$.data.id");

       // 建立第二個預設選項為是的 Option 資料
        OptionCreateDTO secondDefaultOption = new OptionCreateDTO();
        BeanUtils.copyProperties(firstDefaultOption, secondDefaultOption);
        secondDefaultOption.setTitle(TestDataGenerator.getUnique(firstDefaultOption.getTitle()));

        String jsonBody2 = objectMapper.writeValueAsString(secondDefaultOption);
        mockMvc.perform(
                post("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody2)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.title").value(secondDefaultOption.getTitle()))
                .andExpect(jsonPath("$.data.optionType").value(secondDefaultOption.getOptionType()))
                .andExpect(jsonPath("$.data.isDefault").value(secondDefaultOption.getIsDefault()))
                .andExpect(jsonPath("$.data.price").value(secondDefaultOption.getPrice()));

        // 驗證第一個選項已被更新為非預設
        OptionVO firstOption = optionMapper.getById(firstDefaultOptionId);
        assertAll(
                () -> assertEquals(DefaultEnum.NO.getCode(), firstOption.getIsDefault(), "第一個選項應被更新為非預設"),
                () -> assertEquals(firstDefaultOption.getTitle(), firstOption.getTitle(), "第一個選項的 title 應保持不變"),
                () -> assertEquals(firstDefaultOption.getOptionType(), firstOption.getOptionType(), "第一個選項的 optionType 應保持不變"),
                () -> assertEquals(firstDefaultOption.getPrice(), firstOption.getPrice(), "第一個選項的 price 應保持不變")
        );
    }

    @Test
    @DisplayName("[IT] 4001 createOption - 建立選項 OptionType 為 AddOn 預設設定錯誤，回傳 400 及指定訊息")
    void testCreateOptionDefaultSettingError() throws Exception{

        OptionCreateDTO optionCreateDTO = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.EGG);
        optionCreateDTO.setTitle(TestDataGenerator.getUnique(optionCreateDTO.getTitle()));
        optionCreateDTO.setOptionType(OptionTypeEnum.ADD_ON.getCode());
        optionCreateDTO.setIsDefault(DefaultEnum.YES.getCode());

        String jsonBody = objectMapper.writeValueAsString(optionCreateDTO);
        mockMvc.perform(
                post("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 4001 createOption - 建立產品細節選項名稱重複，回傳 409 及指定訊息")
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
    @DisplayName("[IT] 4001 createOption - 非管理員權限進行新增產品細節選項，回傳 403")
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
    @DisplayName("[IT] 4002 pageQueryOption - 分頁查詢成功，回傳 200 及資料")
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

    @Test
    @DisplayName("[IT] 4003 updateOptionById - 修改成功，回傳 200 及資料")
    void testUpdateOptionByIdSuccess() throws Exception{

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.HOT_SPICY);
        optionEditDTO.setTitle(TestDataGenerator.getUnique("update"));

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(optionEditDTO.getId()))
                .andExpect(jsonPath("$.data.title").value(optionEditDTO.getTitle()))
                .andExpect(jsonPath("$.data.optionType").value(optionEditDTO.getOptionType()));
    }

    @Test
    @DisplayName("[IT] 4003 updateOptionById - 修改選項 OptionType 為 AddOn 預設設定錯誤，回傳 400 及指定訊息")
    void testUpdateOptionByIdDefaultSettingError() throws Exception{

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.EGG);
        optionEditDTO.setOptionType(OptionTypeEnum.ADD_ON.getCode());
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 4003 updateOptionById - 將非 AddOn 類型的 Option 修改預設為「是」，此 OptionType 原有預設選項應調整為非預設，修改成功回傳 200 及資料")
    void testUpdateOptionByIdDefaultSettingSuccess() throws Exception{

        // 1. 建立 RiceType 預設為「是」的 Option 資料
        OptionCreateDTO firstDefaultOption = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        firstDefaultOption.setTitle(TestDataGenerator.getUnique(SeedOptionData.PURPLE_RICE.title()));
        firstDefaultOption.setIsDefault(DefaultEnum.YES.getCode());

        String jsonBody = objectMapper.writeValueAsString(firstDefaultOption);
        MvcResult result = mockMvc.perform(
                        post("/options")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        Integer firstOptionId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");

        // 2. 建立 RiceType 預設為「否」的 Option 資料
        OptionCreateDTO secondDefaultOption = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.PURPLE_RICE);
        secondDefaultOption.setTitle(TestDataGenerator.getUnique(SeedOptionData.PURPLE_RICE.title()));
        secondDefaultOption.setIsDefault(DefaultEnum.NO.getCode());

        String jsonBody2 = objectMapper.writeValueAsString(secondDefaultOption);
        MvcResult result2 = mockMvc.perform(
                        post("/options")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody2)
                ).andExpect(status().isOk())
                .andReturn();

        Integer secondOptionId = JsonPath.read(result2.getResponse().getContentAsString(), "$.data.id");

        // 3. 將原預設為「否」的 Option 修改預設設定為「是」
        OptionEditDTO optionEditDTO = new OptionEditDTO();
        BeanUtils.copyProperties(secondDefaultOption, optionEditDTO);
        optionEditDTO.setId(secondOptionId);
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());

        String jsonBodyEdit = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBodyEdit)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(optionEditDTO.getId()))
                .andExpect(jsonPath("$.data.title").value(secondDefaultOption.getTitle()))
                .andExpect(jsonPath("$.data.optionType").value(secondDefaultOption.getOptionType()))
                .andExpect(jsonPath("$.data.isDefault").value(optionEditDTO.getIsDefault()));

        // 4. 驗證 firstOption 已被更新為非預設
        OptionVO firstOptionAfterEdit = optionMapper.getById(firstOptionId);
        assertAll(
                () -> assertEquals(DefaultEnum.NO.getCode(), firstOptionAfterEdit.getIsDefault(), "第一個選項應被更新為非預設"),
                () -> assertEquals(firstDefaultOption.getTitle(), firstOptionAfterEdit.getTitle(), "第一個選項的 title 應保持不變"),
                () -> assertEquals(firstDefaultOption.getOptionType(), firstOptionAfterEdit.getOptionType(), "第一個選項的 optionType 應保持不變"),
                () -> assertEquals(firstDefaultOption.getPrice(), firstOptionAfterEdit.getPrice(), "第一個選項的 price 應保持不變")
        );
    }

    @Test
    @DisplayName("[IT] 4003 updateOptionById - 非 AddOn 類型的 Option 修改預設為「是」，且同時修改 optionType,應將修改後 Type 的原有預設選項調整為「非預設」，修改成功回傳 200 及資料")
    void testUpdateOptionByIdDefaultSettingWithOptionTypeChangeSuccess() throws Exception{
        // 1. 建立 Rice Size 類型的預設選項設定為「是」的 Option 資料
        OptionCreateDTO firstDefaultOption = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.LARGE_SIZE);
        firstDefaultOption.setTitle(TestDataGenerator.getUnique(SeedOptionData.LARGE_SIZE.title()));
        firstDefaultOption.setOptionType(OptionTypeEnum.RICE_SIZE.getCode());
        firstDefaultOption.setIsDefault(DefaultEnum.YES.getCode());

        String jsonBody = objectMapper.writeValueAsString(firstDefaultOption);
        MvcResult result = mockMvc.perform(
                        post("/options")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isOk())
                .andReturn();

        Integer firstOptionId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");

        // 2. 建立 AddOn 類型的預設選項為「否」的 Option 資料
        OptionCreateDTO secondOption = OptionTestDataFactory.getOptionCreateDTO(SeedOptionData.EGG);
        secondOption.setTitle(TestDataGenerator.getUnique(SeedOptionData.EGG.title()));
        secondOption.setOptionType(OptionTypeEnum.ADD_ON.getCode());
        secondOption.setIsDefault(DefaultEnum.NO.getCode());

        String jsonBody2 = objectMapper.writeValueAsString(secondOption);
        MvcResult result2 = mockMvc.perform(
                        post("/options")
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody2)
                ).andExpect(status().isOk())
                .andReturn();

        Integer secondOptionId = JsonPath.read(result2.getResponse().getContentAsString(), "$.data.id");

        // 3. 修改 secondOption 預設設定為「是」並修改 optionType 為 Rice Size
        OptionEditDTO optionEditDTO = new OptionEditDTO();
        BeanUtils.copyProperties(secondOption, optionEditDTO);
        optionEditDTO.setId(secondOptionId);
        optionEditDTO.setIsDefault(DefaultEnum.YES.getCode());
        optionEditDTO.setOptionType(OptionTypeEnum.RICE_SIZE.getCode());

        String jsonBodyEdit = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBodyEdit)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.id").value(optionEditDTO.getId()))
                .andExpect(jsonPath("$.data.title").value(secondOption.getTitle()))
                .andExpect(jsonPath("$.data.optionType").value(optionEditDTO.getOptionType()))
                .andExpect(jsonPath("$.data.isDefault").value(optionEditDTO.getIsDefault()));

        // 4. 驗證 firstOption 已被更新為非預設
        OptionVO firstOptionAfterEdit = optionMapper.getById(firstOptionId);
        assertAll(
                () -> assertEquals(DefaultEnum.NO.getCode(), firstOptionAfterEdit.getIsDefault(), "第一個選項應被更新為非預設"),
                () -> assertEquals(firstDefaultOption.getTitle(), firstOptionAfterEdit.getTitle(), "第一個選項的 title 應保持不變"),
                () -> assertEquals(firstDefaultOption.getOptionType(), firstOptionAfterEdit.getOptionType(), "第一個選項的 optionType 應保持不變"),
                () -> assertEquals(firstDefaultOption.getPrice(), firstOptionAfterEdit.getPrice(), "第一個選項的 price 應保持不變")
        );

    }

    @Test
    @DisplayName("[IT] 4003 updateOptionById - 修改選項 id 不存在，回傳 404")
    void testUpdateOptionByIdNoExist() throws Exception{

        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.LARGE_SIZE);
        optionEditDTO.setId(Integer.MAX_VALUE);

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);
        mockMvc.perform(
                put("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_NOT_EXIST.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 4003 updateOptionById - 修改選項名稱已存在，回傳 409")
    void testUpdateOptionByIdTitleDuplicate() throws Exception{
        OptionEditDTO optionEditDTO = OptionTestDataFactory.getOptionEditDTO(SeedOptionData.HOT_SPICY);
        optionEditDTO.setTitle(SeedOptionData.COLD.title());

        String jsonBody = objectMapper.writeValueAsString(optionEditDTO);

        mockMvc.perform(
                put("/options")
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_ALREADY_EXISTS.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("[IT] 4005 getActiveOptionsByType - 查詢成功，回傳 200 及資料")
    void testGetActiveOptionsByTypeSuccess() throws Exception{
        OptionTypeEnum optionType = OptionTypeEnum.RICE_SIZE;

        mockMvc.perform(
                get("/options/active")
                        .header("Authorization", "Bearer " + tokenManager)
                        .param("optionType", String.valueOf(optionType.getCode()))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(MessageEnum.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].optionType").value(everyItem(equalTo(optionType.getCode()))))
                .andExpect(jsonPath("$.data[*].status").value(everyItem(equalTo(StatusEnum.ACTIVE.getCode()))));
    }

    @Test
    @DisplayName("[IT] 4006 updateOptionStatus - 修改成功，回傳 200")
    void testUpdateOptionStatusSuccess() throws Exception{
        OptionVO optionVO = optionMapper.getById(SeedOptionData.HOT_SPICY.id());
        OptionStatusDTO optionStatusDTO = new OptionStatusDTO();
        optionStatusDTO.setStatus(StatusEnum.INACTIVE.getCode());

        String jsonBody = objectMapper.writeValueAsString(optionStatusDTO);
        mockMvc.perform(
                patch("/options/{id}/status", optionVO.getId())
                        .header("Authorization", "Bearer " + tokenManager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        ).andExpect(status().isOk());

        OptionVO updatedOption = optionMapper.getById(optionVO.getId());
        assertEquals(optionStatusDTO.getStatus(), updatedOption.getStatus(), "選項狀態應被更新為 Inactive");
    }

    @Test
    @DisplayName("[IT] 4006 updateOptionStatus - 修改選項狀態時 id 不存在，回傳 404")
    void testUpdateOptionStatusNoExist() throws Exception {
        OptionStatusDTO optionStatusDTO = new OptionStatusDTO();
        optionStatusDTO.setStatus(StatusEnum.INACTIVE.getCode());

        String jsonBody = objectMapper.writeValueAsString(optionStatusDTO);
        mockMvc.perform(
                        patch("/options/{id}/status", Integer.MAX_VALUE)
                                .header("Authorization", "Bearer " + tokenManager)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody)
                ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageEnum.OPTION_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.msg").value(MessageEnum.OPTION_NOT_EXIST.getMessage()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

}
