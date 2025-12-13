package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    
    /**
     * 查询超时未支付的订单
     * @param timeout 超时时间（分钟）
     * @return 超时订单列表
     */
    @Select("SELECT * FROM orders WHERE status = 0 AND created_at < DATE_SUB(NOW(), INTERVAL #{timeout} MINUTE)")
    List<Order> selectTimeoutPendingPaymentOrders(@Param("timeout") int timeout);
    
    /**
     * 查询超时未接单的订单
     * @param timeout 超时时间（分钟）
     * @return 超时订单列表
     */
    @Select("SELECT * FROM orders WHERE status = 1 AND pay_time < DATE_SUB(NOW(), INTERVAL #{timeout} MINUTE)")
    List<Order> selectTimeoutPendingAcceptOrders(@Param("timeout") int timeout);
}