package com.snackchain.snack_platform_backend.module.store.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.module.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店控制器（消费者端）
 */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {
    
    private final StoreService storeService;
    
    /**
     * 获取所有营业中的门店列表
     */
    @GetMapping
    public Result<List<Store>> list() {
        List<Store> stores = storeService.listOpen();
        return Result.success(stores);
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
     * 获取附近门店
     */
    @GetMapping("/nearby")
    public Result<List<Store>> nearby(
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Store> stores = storeService.listNearby(longitude, latitude, limit);
        return Result.success(stores);
    }
}