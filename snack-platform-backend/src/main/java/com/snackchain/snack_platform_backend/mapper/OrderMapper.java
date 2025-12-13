package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 根据订单编号查询订单
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);
    
    /**
     * 根据自提码查询订单
     */
    @Select("SELECT * FROM orders WHERE pickup_code = #{pickupCode} AND store_id = #{storeId}")
    Order selectByPickupCodeAndStoreId(String pickupCode, Long storeId);
}