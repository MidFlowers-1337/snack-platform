package com.snackchain.snack_platform_backend.module.product.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 热销商品推荐VO
 */
@Data
public class HotProductVO {
    /**
     * 商品ID（平台SPU ID）
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 总销量
     */
    private Long totalSold;
}
