package com.qqriceball.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.exception.ResourceNotFoundException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.DefaultEnum;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.option.*;
import com.qqriceball.model.entity.Option;
import com.qqriceball.model.vo.OptionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OptionService {

    private final OptionMapper optionMapper;

    @Autowired
    public OptionService(OptionMapper optionMapper) {
        this.optionMapper = optionMapper;
    }

    @Transactional
    public OptionVO create(OptionCreateDTO optionCreateDTO) {

        this.checkDefaultSetting(optionCreateDTO.getOptionType(), optionCreateDTO.getIsDefault());

        Option option = new Option();
        BeanUtils.copyProperties(optionCreateDTO, option);

        try {
            // 如果預設設定為是，則清除同類型的其他選項的預設設定
            if (option.getIsDefault() == DefaultEnum.YES.getCode()) {
                optionMapper.cleanDefaultByOptionType(option.getOptionType());
            }

            optionMapper.insert(option);
            return optionMapper.getById(option.getId());

        } catch (
                DuplicateKeyException e){
            log.error("建立產品細節選項名稱已存在,title: {}",optionCreateDTO.getTitle(),e);
            throw new AlreadyExistsException(MessageEnum.OPTION_ALREADY_EXISTS);
        }
    }

    public PageResult pageQuery(OptionPageQueryDTO optionPageQueryDTO) {
            PageHelper.startPage(optionPageQueryDTO.getPage(),
                    optionPageQueryDTO.getPageSize());

            List<OptionVO> list = optionMapper.pageQuery(optionPageQueryDTO);

            Page<OptionVO> page = (Page<OptionVO>) list;

            return new PageResult(page.getTotal(), optionPageQueryDTO.getPage(),
                    optionPageQueryDTO.getPageSize(), page.getResult());
    }

    @Transactional
    public OptionVO updateById(OptionEditDTO optionEditDTO) {

        OptionVO optionVO = this.getById(optionEditDTO.getId());

        Option option = new Option();
        BeanUtils.copyProperties(optionEditDTO, option);

        if (option.getIsDefault() == DefaultEnum.YES.getCode()) {
            // 確認是否有修改選項類型，如果有修改則以修改後的選項類型為準進行預設設定檢查，否則以原選項類型為準
            Integer finalOptionType = option.getOptionType() != null
                    ? option.getOptionType() : optionVO.getOptionType();
            option.setOptionType(finalOptionType);
            this.checkDefaultSetting(option.getOptionType(), option.getIsDefault());
        }

        try{

            // 如果預設設定為是，則清除同類型的其他選項的預設設定
            if(option.getIsDefault() == DefaultEnum.YES.getCode()){
                optionMapper.cleanDefaultByOptionType(option.getOptionType());
            }

            optionMapper.updateById(option);

            return optionMapper.getById(option.getId());
        }catch (DuplicateKeyException e) {
            log.error("編輯產品細節選項名稱已存在,title: {}", option.getTitle(), e);
            throw new AlreadyExistsException(MessageEnum.OPTION_ALREADY_EXISTS);
        }
    }

    public OptionVO getById(Integer id) {
        OptionVO optionVO = optionMapper.getById(id);

        if (optionVO == null) {
            log.error("查無資料,ID: {}", id);
            throw new ResourceNotFoundException(MessageEnum.OPTION_NOT_EXIST);
        }else {
            return optionVO;
        }
    }

    public void updateStatus(Integer id, OptionStatusDTO optionStatusDTO){
        this.getById(id);

        Option option = new Option();
        option.setId(id);
        option.setStatus(optionStatusDTO.getStatus());
        optionMapper.updateById(option);
    }

    private void checkDefaultSetting(Integer optionType, Integer defaultSetting) {
        if (optionType == OptionTypeEnum.ADD_ON.getCode() && defaultSetting == DefaultEnum.YES.getCode()) {
            log.error("加料類選項不可設為預設, defaultSetting: {}", defaultSetting);
            throw new BadRequestArgsException(MessageEnum.OPTION_ADD_ON_DEFAULT_ERROR);
        }

        if(optionType == OptionTypeEnum.NO_INGREDIENT.getCode() && defaultSetting == DefaultEnum.YES.getCode()){
            log.error("去除配料類選項不可設為預設, defaultSetting: {}", defaultSetting);
            throw new BadRequestArgsException(MessageEnum.OPTION_NO_INGREDIENT_DEFAULT_ERROR);
        }
    }


}
