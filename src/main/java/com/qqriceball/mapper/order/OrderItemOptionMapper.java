package com.qqriceball.mapper.order;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.entity.order.OrderItemOption;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemOptionMapper {

    void insert(OrderItemOption orderItemOption);
}
