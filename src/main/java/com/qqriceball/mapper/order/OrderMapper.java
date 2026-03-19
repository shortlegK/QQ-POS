package com.qqriceball.mapper.order;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.entity.order.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    @Select("SELECT order_no FROM orders " +
            "WHERE DATE_FORMAT(pickup_time, '%Y%m%d') >= #{start} " +
            "AND DATE_FORMAT(pickup_time, '%Y%m%d') < #{end} " +
            "ORDER BY order_no DESC LIMIT 1")
    String getMaxOrderNoByPickupTime(String start,String end);

    @AutoFill(value = OperationType.INSERT)
    void insert(Order order);
}
