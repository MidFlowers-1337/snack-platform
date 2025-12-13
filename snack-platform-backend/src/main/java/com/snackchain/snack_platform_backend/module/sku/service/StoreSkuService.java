package com.snackchain.snack_platform_backend.module.sku.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.entity.StoreSku;

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店商品服务接口
 */
public interface StoreSkuService {
    
    /**
     * 获取门店上架商品列表（消费者端）
     */
    List<StoreSku> listByStoreId(Long storeId);
    
    /**
     * 根据门店和分类获取商品列表
     */
    List<StoreSku> listByStoreIdAndCategoryId(Long storeId, Long categoryId);
    
    /**
     * 分页获取门店商品（门店管理端）
     */
    IPage<StoreSku> pageByStoreId(Long storeId, int pageNum, int pageSize, String keyword, Integer status);
    
    /**
     * 根据ID获取门店商品
     */
    StoreSku getById(Long id);
    
    /**
     * 根据门店ID和商品ID获取SKU
     */
    StoreSku getByStoreIdAndProductId(Long storeId, Long productId);
    
    /**
     * 添加门店商品
     */
    void create(Long storeId, Long productId, BigDecimal price, Integer stock);
    
    /**
     * 更新门店商品
     */
    void update(Long id, BigDecimal price, Integer stock, Integer status);
    
    /**
     * 删除门店商品
     */
    void delete(Long id);
    
    /**
     * 上架门店商品
     */
    void onShelf(Long id);
    
    /**
     * 下架门店商品
     */
    void offShelf(Long id);
    
    /**
     * 扣减库存
     */
    boolean deductStock(Long id, Integer quantity);
    
    /**
     * 恢复库存
     */
    void restoreStock(Long id, Integer quantity);
}