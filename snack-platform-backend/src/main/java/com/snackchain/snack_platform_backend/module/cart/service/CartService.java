package com.snackchain.snack_platform_backend.module.cart.service;

import com.snackchain.snack_platform_backend.module.cart.dto.CartItemDTO;
import com.snackchain.snack_platform_backend.module.cart.dto.UpdateCartDTO;
import com.snackchain.snack_platform_backend.module.cart.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 获取当前用户购物车列表
     */
    List<CartItemVO> listCartItems();

    /**
     * 添加商品到购物车
     */
    void addItem(CartItemDTO dto);

    /**
     * 更新购物车项数量
     */
    void updateItem(Long cartItemId, UpdateCartDTO dto);

    /**
     * 删除购物车项
     */
    void removeItem(Long cartItemId);

    /**
     * 清空指定门店的购物车
     */
    void clearByStore(Long storeId);
}
