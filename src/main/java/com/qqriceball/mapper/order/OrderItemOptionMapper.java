package com.qqriceball.mapper.order;

import com.qqriceball.model.entity.order.OrderItemOption;
import com.qqriceball.model.vo.order.OrderItemOptionVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemOptionMapper {

    void insert(OrderItemOption orderItemOption);

    List<OrderItemOptionVO> getOptionsByItemId(Integer itemId);

    @Delete("DELETE FROM order_item_options WHERE order_item_id = #{itemId}")
    void deleteOptionsByItemId(Integer itemId);

}
