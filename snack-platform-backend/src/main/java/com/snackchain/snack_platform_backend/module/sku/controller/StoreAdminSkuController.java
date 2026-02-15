package com.snackchain.snack_platform_backend.module.sku.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.StoreSku;
import com.snackchain.snack_platform_backend.module.sku.dto.StoreSkuDTO;
import com.snackchain.snack_platform_backend.module.sku.service.StoreSkuService;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门店商品管理控制器（门店管理员端）
 */
@RestController
@RequestMapping("/store/skus")
@RequiredArgsConstructor
@Tag(name = "门店商品管理-门店")
public class StoreAdminSkuController {
    
    private final StoreSkuService storeSkuService;
    
    /**
     * 获取当前门店的商品列表
     */
    @Operation(summary = "获取当前门店的商品列表")
    @GetMapping
    public Result<IPage<StoreSku>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Long storeId = getCurrentStoreId();
        IPage<StoreSku> page = storeSkuService.pageByStoreId(storeId, pageNum, pageSize, keyword, status);
        return Result.success(page);
    }
    
    /**
     * 获取门店商品详情
     */
    @Operation(summary = "获取门店商品详情")
    @GetMapping("/{id}")
    public Result<StoreSku> getById(@PathVariable Long id) {
        StoreSku sku = storeSkuService.getById(id);
        // 验证是否属于当前门店
        validateStoreOwnership(sku);
        return Result.success(sku);
    }
    
    /**
     * 添加门店商品
     */
    @Operation(summary = "添加门店商品")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody StoreSkuDTO dto) {
        Long storeId = getCurrentStoreId();
        storeSkuService.create(storeId, dto.getProductId(), dto.getPrice(), dto.getStock());
        return Result.success();
    }
    
    /**
     * 更新门店商品
     */
    @Operation(summary = "更新门店商品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StoreSkuDTO dto) {
        StoreSku sku = storeSkuService.getById(id);
        validateStoreOwnership(sku);
        storeSkuService.update(id, dto.getPrice(), dto.getStock(), dto.getStatus());
        return Result.success();
    }
    
    /**
     * 删除门店商品
     */
    @Operation(summary = "删除门店商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StoreSku sku = storeSkuService.getById(id);
        validateStoreOwnership(sku);
        storeSkuService.delete(id);
        return Result.success();
    }
    
    /**
     * 上架门店商品
     */
    @Operation(summary = "上架门店商品")
    @PutMapping("/{id}/on-shelf")
    public Result<Void> onShelf(@PathVariable Long id) {
        StoreSku sku = storeSkuService.getById(id);
        validateStoreOwnership(sku);
        storeSkuService.onShelf(id);
        return Result.success();
    }
    
    /**
     * 下架门店商品
     */
    @Operation(summary = "下架门店商品")
    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        StoreSku sku = storeSkuService.getById(id);
        validateStoreOwnership(sku);
        storeSkuService.offShelf(id);
        return Result.success();
    }
    
    /**
     * 获取当前门店ID
     */
    private Long getCurrentStoreId() {
        Long storeId = UserContextHolder.getStoreId();
        if (storeId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您未绑定门店");
        }
        return storeId;
    }
    
    /**
     * 验证门店归属
     */
    private void validateStoreOwnership(StoreSku sku) {
        Long currentStoreId = getCurrentStoreId();
        if (!currentStoreId.equals(sku.getStoreId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作其他门店的商品");
        }
    }
}