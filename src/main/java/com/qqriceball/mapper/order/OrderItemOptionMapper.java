package com.qqriceball.mapper.order;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.entity.order.OrderItemOption;
import com.qqriceball.model.vo.order.OrderItemOptionVO;
import com.qqriceball.model.vo.order.OrderItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemOptionMapper {

    void insert(OrderItemOption orderItemOption);

    List<OrderItemOptionVO> getOptionsByItemId(Integer itemId);

}
