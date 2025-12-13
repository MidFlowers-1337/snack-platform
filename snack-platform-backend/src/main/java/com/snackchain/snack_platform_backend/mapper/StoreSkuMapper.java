package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.StoreSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 门店商品Mapper
 */
@Mapper
public interface StoreSkuMapper extends BaseMapper<StoreSku> {
    
    /**
     * 检查门店商品是否存在
     */
    @Select("SELECT COUNT(*) FROM store_skus WHERE store_id = #{storeId} AND product_id = #{productId}")
    int countByStoreIdAndProductId(Long storeId, Long productId);
}