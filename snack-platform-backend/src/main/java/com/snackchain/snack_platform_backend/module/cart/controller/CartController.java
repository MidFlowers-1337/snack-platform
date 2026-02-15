package com.snackchain.snack_platform_backend.module.cart.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.module.cart.dto.CartItemDTO;
import com.snackchain.snack_platform_backend.module.cart.dto.UpdateCartDTO;
import com.snackchain.snack_platform_backend.module.cart.service.CartService;
import com.snackchain.snack_platform_backend.module.cart.vo.CartItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 * 消费者端购物车管理接口
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "购物车")
public class CartController {

    private final CartService cartService;

    /**
     * 获取当前用户购物车列表
     */
    @Operation(summary = "获取购物车列表")
    @GetMapping
    public Result<List<CartItemVO>> list() {
        List<CartItemVO> items = cartService.listCartItems();
        return Result.success(items);
    }

    /**
     * 添加商品到购物车
     */
    @Operation(summary = "添加商品到购物车")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartItemDTO dto) {
        cartService.addItem(dto);
        return Result.success();
    }

    /**
     * 更新购物车项数量
     */
    @Operation(summary = "更新购物车项数量")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateCartDTO dto) {
        cartService.updateItem(id, dto);
        return Result.success();
    }

    /**
     * 删除购物车项
     */
    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.removeItem(id);
        return Result.success();
    }

    /**
     * 清空指定门店的购物车
     */
    @Operation(summary = "清空门店购物车")
    @DeleteMapping("/clear/{storeId}")
    public Result<Void> clearByStore(@PathVariable Long storeId) {
        cartService.clearByStore(storeId);
        return Result.success();
    }
}
