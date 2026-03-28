package com.qqriceball.mapper;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.dto.option.OptionPageQueryDTO;
import com.qqriceball.model.entity.Option;
import com.qqriceball.model.vo.option.OptionVO;
import com.qqriceball.model.vo.order.catalog.OrderableOptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OptionMapper {

    @Select("select id, title, option_type,is_default, price, status from options where id = #{id}")
    OptionVO getById(Integer id);

    @AutoFill(value = OperationType.INSERT)
    void  insert(Option option);

    List<OptionVO> pageQuery(OptionPageQueryDTO optionPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void updateById(Option option);

    @AutoFill(value = OperationType.UPDATE)
    void cleanDefaultByOptionType(Integer optionType);

    @Select("select id, title, option_type, price, is_default from options where status = 1 and option_type = #{optionType}")
    List<OrderableOptionVO> getActiveOptionsByType(Integer optionType);

}
