package com.qqriceball.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.BadRequestArgsException;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.OptionCreateDTO;
import com.qqriceball.model.dto.OptionPageQueryDTO;
import com.qqriceball.model.entity.Option;
import com.qqriceball.model.vo.OptionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OptionService {

    private final OptionMapper optionMapper;

    public OptionService(OptionMapper optionMapper) {
        this.optionMapper = optionMapper;
    }

    public OptionVO create(OptionCreateDTO optionCreateDTO) {

        Option option = new Option();
        BeanUtils.copyProperties(optionCreateDTO, option);

        try {
            optionMapper.insert(option);
            return optionMapper.getById(option.getId());

        } catch (
                DuplicateKeyException e){
            log.error("建立產品細節選項名稱已存在,title: {}",optionCreateDTO.getTitle(),e);
            throw new AlreadyExistsException(MessageEnum.OPTION_ALREADY_EXISTS);
        }
    }

    public PageResult pageQuery(OptionPageQueryDTO optionPageQueryDTO) {
        try{
            PageHelper.startPage(optionPageQueryDTO.getPage(),
                    optionPageQueryDTO.getPageSize());

            List<OptionVO> list = optionMapper.pageQuery(optionPageQueryDTO);

            Page<OptionVO> page = (Page<OptionVO>) list;

            return new PageResult(page.getTotal(), optionPageQueryDTO.getPage(),
                    optionPageQueryDTO.getPageSize(), page.getResult());

        }catch (Exception e) {
            log.error("查詢產品細節選項異常：{}", optionPageQueryDTO, e);
            throw new BadRequestArgsException(MessageEnum.BAD_REQUEST);
        }

    }

}
