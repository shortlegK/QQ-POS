package com.qqriceball.mapper.order;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.dto.order.OrderPageQueryDTO;
import com.qqriceball.model.entity.order.Order;
import com.qqriceball.model.vo.order.OrderDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("SELECT order_no FROM orders " +
            "WHERE order_no LIKE CONCAT(#{pickedDate},'%')" +
            "ORDER BY order_no DESC LIMIT 1")
    String getMaxOrderNoByPickupTime(String pickedDate);

    @AutoFill(value = OperationType.INSERT)
    void insert(Order order);

    List<OrderDetailVO> pageQuery(OrderPageQueryDTO orderPageQueryDTO);

    OrderDetailVO getByOrderNo(String orderNo);

    @AutoFill(value = OperationType.UPDATE)
    void updateByOrderNo(Order order);

}
