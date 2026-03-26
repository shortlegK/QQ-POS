package com.qqriceball.utils.option;

import com.qqriceball.model.dto.option.OptionCreateDTO;
import com.qqriceball.model.dto.option.OptionEditDTO;
import com.qqriceball.model.dto.option.OptionPageQueryDTO;
import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.testData.option.TestOption;
import org.springframework.beans.BeanUtils;

public class OptionTestDataFactory {

    public static OptionCreateDTO getOptionCreateDTO(TestOption option){
        OptionCreateDTO optionCreateDTO = new OptionCreateDTO();
        BeanUtils.copyProperties(option, optionCreateDTO);
        return optionCreateDTO;
    }

    public static OptionEditDTO getOptionEditDTO(TestOption option){
        OptionEditDTO optionEditDTO = new OptionEditDTO();
        BeanUtils.copyProperties(option, optionEditDTO);
        return optionEditDTO;
    }

    public static OptionPageQueryDTO getOptionPageQueryDTO(Integer page, Integer pageSize, String title, Integer optionType, Integer status){
        OptionPageQueryDTO optionPageQueryDTO = new OptionPageQueryDTO();
        optionPageQueryDTO.setPage(page);
        optionPageQueryDTO.setPageSize(pageSize);
        optionPageQueryDTO.setTitle(title);
        optionPageQueryDTO.setOptionType(optionType);
        optionPageQueryDTO.setStatus(status);
        return optionPageQueryDTO;
    }

    public static OptionVO getOptionVO(TestOption option){
        OptionVO optionVO = new OptionVO();
        BeanUtils.copyProperties(option, optionVO);
        return optionVO;
    }


}
