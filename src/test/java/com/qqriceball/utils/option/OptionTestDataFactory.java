package com.qqriceball.utils.option;

import com.qqriceball.model.dto.OptionCreateDTO;
import com.qqriceball.model.vo.OptionVO;
import com.qqriceball.testData.option.TestOption;
import org.springframework.beans.BeanUtils;

public class OptionTestDataFactory {

    public static OptionCreateDTO getOptionCreateDTO(TestOption option){
        OptionCreateDTO optionCreateDTO = new OptionCreateDTO();
        BeanUtils.copyProperties(option, optionCreateDTO);
        return optionCreateDTO;
    }

//    public static ProductEditDTO getProductEditDTO(TestProduct product){
//        ProductEditDTO productEditDTO = new ProductEditDTO();
//        BeanUtils.copyProperties(product, productEditDTO);
//        return productEditDTO;
//    }

//    public static ProductPageQueryDTO getProductPageQueryDTO(Integer page, Integer pageSize, String title){
//        ProductPageQueryDTO productPageQueryDTO = new ProductPageQueryDTO();
//        productPageQueryDTO.setPage(page);
//        productPageQueryDTO.setPageSize(pageSize);
//        productPageQueryDTO.setTitle(title);
//        return productPageQueryDTO;
//    }
//
//    public static ProductPageQueryVO getProductPageQueryVO(TestProduct product){
//        ProductPageQueryVO productPageQueryVO = new ProductPageQueryVO();
//        BeanUtils.copyProperties(product, productPageQueryVO);
//        return productPageQueryVO;
//    }

    public static OptionVO getOptionVO(TestOption option){
        OptionVO optionVO = new OptionVO();
        BeanUtils.copyProperties(option, optionVO);
        return optionVO;
    }

}
