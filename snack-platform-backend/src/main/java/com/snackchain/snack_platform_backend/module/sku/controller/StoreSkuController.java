package com.snackchain.snack_platform_backend.module.sku.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.StoreSku;
import com.snackchain.snack_platform_backend.module.sku.service.StoreSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店商品控制器（消费者端）
 */
@RestController
@RequestMapping("/stores/{storeId}/skus")
@RequiredArgsConstructor
public class StoreSkuController {
    
    private final StoreSkuService storeSkuService;
    
    /**
     * 获取门店商品列表
     */
    @GetMapping
    public Result<List<StoreSku>> list(
            @PathVariable Long storeId,
            @RequestParam(required = false) String keyword) {
        List<StoreSku> skus = storeSkuService.listByStoreId(storeId, keyword);
        return Result.success(skus);
    }
    
    /**
     * 根据分类获取门店商品列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<StoreSku>> listByCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId) {
        List<StoreSku> skus = storeSkuService.listByStoreIdAndCategoryId(storeId, categoryId);
        return Result.success(skus);
    }
    
    /**
     * 获取门店商品详情
     */
    @GetMapping("/{skuId}")
    public Result<StoreSku> getById(@PathVariable Long storeId, @PathVariable Long skuId) {
        StoreSku sku = storeSkuService.getById(skuId);
        return Result.success(sku);
    }
}