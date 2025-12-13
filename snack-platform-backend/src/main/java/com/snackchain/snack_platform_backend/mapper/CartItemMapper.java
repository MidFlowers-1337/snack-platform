package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车项Mapper
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
    
}