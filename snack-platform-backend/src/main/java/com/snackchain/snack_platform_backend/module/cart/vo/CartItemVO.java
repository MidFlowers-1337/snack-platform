package com.snackchain.snack_platform_backend.module.cart.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项视图对象
 * 返回给前端的购物车数据，包含商品详细信息
 */
@Data
@Builder
public class CartItemVO {

    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店商品ID（SKU ID）
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 门店售价
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 门店名称
     */
    private String storeName;
}
