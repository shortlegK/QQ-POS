package com.qqriceball.service;


import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.dto.OptionCreateDTO;
import com.qqriceball.model.entity.Option;
import com.qqriceball.model.vo.OptionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

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
            log.error("建立選項品項名稱已存在,title: {}",optionCreateDTO.getTitle(),e);
            throw new AlreadyExistsException(MessageEnum.OPTION_ALREADY_EXISTS);
        }
    }


}
