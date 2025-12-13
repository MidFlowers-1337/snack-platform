package com.snackchain.snack_platform_backend.module.store.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.module.store.dto.StoreDTO;
import com.snackchain.snack_platform_backend.module.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门店管理控制器（系统管理员端）
 */
@RestController
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {
    
    private final StoreService storeService;
    
    /**
     * 分页获取门店列表
     */
    @GetMapping
    public Result<IPage<Store>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<Store> page = storeService.page(pageNum, pageSize, keyword);
        return Result.success(page);
    }
    
    /**
     * 获取门店详情
     */
    @GetMapping("/{id}")
    public Result<Store> getById(@PathVariable Long id) {
        Store store = storeService.getById(id);
        return Result.success(store);
    }
    
    /**
     * 创建门店
     */
    @PostMapping
    @OperationLog(module = "门店管理", operation = "创建门店")
    public Result<Void> create(@Valid @RequestBody StoreDTO dto) {
        storeService.create(dto.getName(), dto.getAddress(), dto.getPhone(), dto.getImage(),
                dto.getLongitude(), dto.getLatitude(), dto.getBusinessHours());
        return Result.success();
    }
    
    /**
     * 更新门店
     */
    @PutMapping("/{id}")
    @OperationLog(module = "门店管理", operation = "更新门店")
    public Result<Void> update(@PathVariable Long id, @RequestBody StoreDTO dto) {
        storeService.update(id, dto.getName(), dto.getAddress(), dto.getPhone(), dto.getImage(),
                dto.getLongitude(), dto.getLatitude(), dto.getBusinessHours(), dto.getStatus());
        return Result.success();
    }
    
    /**
     * 删除门店
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "门店管理", operation = "删除门店")
    public Result<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return Result.success();
    }
}