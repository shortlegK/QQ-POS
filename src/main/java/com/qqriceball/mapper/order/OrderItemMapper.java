package com.qqriceball.mapper.order;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.entity.order.OrderItem;
import com.qqriceball.model.vo.order.OrderItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    void insert(OrderItem orderItem);

    List<OrderItemVO> getItemsByOrderId(Integer orderId);
}
