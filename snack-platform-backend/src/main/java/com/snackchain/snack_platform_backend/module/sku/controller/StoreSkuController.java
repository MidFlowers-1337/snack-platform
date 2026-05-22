package com.snackchain.snack_platform_backend.module.sku.controller;

import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.StoreSku;
import com.snackchain.snack_platform_backend.module.sku.service.StoreSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店商品控制器（消费者端）
 */
@RestController
@RequestMapping("/stores/{storeId}/skus")
@RequiredArgsConstructor
@Tag(name = "门店商品-消费者")
public class StoreSkuController {
    
    private final StoreSkuService storeSkuService;
    
    /**
     * 获取门店商品列表
     */
    @Operation(summary = "获取门店商品列表")
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
    @Operation(summary = "根据分类获取门店商品列表")
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
    @Operation(summary = "获取门店商品详情")
    @GetMapping("/{skuId}")
    public Result<StoreSku> getById(@PathVariable Long storeId, @PathVariable Long skuId) {
        StoreSku sku = storeSkuService.getById(skuId);
        if (!storeId.equals(sku.getStoreId())) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }
        return Result.success(sku);
    }
}
