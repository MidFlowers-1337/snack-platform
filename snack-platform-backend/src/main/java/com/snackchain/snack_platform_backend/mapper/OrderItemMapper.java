package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单项Mapper
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    /**
     * 根据订单ID查询订单项
     */
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(Long orderId);
}