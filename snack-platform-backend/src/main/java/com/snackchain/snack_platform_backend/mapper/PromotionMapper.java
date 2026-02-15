package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.Promotion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PromotionMapper extends BaseMapper<Promotion> {

    /**
     * 乐观锁扣减促销库存（CAS防超卖）
     */
    @Update("UPDATE promotions SET sold_count = sold_count + #{quantity} " +
            "WHERE id = #{id} AND (stock - sold_count) >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);
}
