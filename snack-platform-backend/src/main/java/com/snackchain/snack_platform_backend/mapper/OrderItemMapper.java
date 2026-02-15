package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.OrderItem;
import com.snackchain.snack_platform_backend.module.product.vo.HotProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 根据多个订单ID批量查询订单项（解决 N+1 查询问题）
     */
    List<OrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 查询热销商品排行（基于order_items销量统计）
     * 通过store_skus关联获取平台商品ID，并从products表获取商品图片
     * 仅统计有效订单（状态>=1 且 非取消状态5）
     */
    @Select("""
        SELECT sk.product_id AS productId,
               oi.product_name AS productName,
               p.image AS image,
               MIN(oi.price) AS price,
               SUM(oi.quantity) AS totalSold
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        JOIN store_skus sk ON oi.sku_id = sk.id
        JOIN products p ON sk.product_id = p.id
        WHERE o.status >= 1 AND o.status != 5
        GROUP BY sk.product_id, oi.product_name, p.image
        ORDER BY totalSold DESC
        LIMIT #{limit}
        """)
    List<HotProductVO> selectHotProducts(@Param("limit") int limit);
}