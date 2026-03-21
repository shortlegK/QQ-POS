package com.qqriceball.mapper.order;

import com.qqriceball.annotation.AutoFill;
import com.qqriceball.enumeration.OperationType;
import com.qqriceball.model.dto.order.OrderPageQueryDTO;
import com.qqriceball.model.entity.order.Order;
import com.qqriceball.model.vo.order.OrderDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("SELECT order_no FROM orders " +
            "WHERE DATE_FORMAT(pickup_time, '%Y%m%d') >= #{start} " +
            "AND DATE_FORMAT(pickup_time, '%Y%m%d') < #{end} " +
            "ORDER BY order_no DESC LIMIT 1")
    String getMaxOrderNoByPickupTime(String start,String end);

    @AutoFill(value = OperationType.INSERT)
    void insert(Order order);

    List<OrderDetailVO> pageQuery(OrderPageQueryDTO orderPageQueryDTO);

    OrderDetailVO getByOrderNo(String orderNo);

    @AutoFill(value = OperationType.UPDATE)
    void updateByOrderNo(Order order);

    @AutoFill(value = OperationType.UPDATE)
    @Update("UPDATE orders SET status = #{status} WHERE order_no = #{orderNo}")
    void updateStatusByOrderNo(String orderNo, Integer status);

}
