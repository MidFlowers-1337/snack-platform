package com.snackchain.snack_platform_backend.module.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.entity.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {
    
    /**
     * 获取所有上架商品列表
     */
    List<Product> listOnShelf();
    
    /**
     * 根据分类获取上架商品列表
     */
    List<Product> listByCategoryId(Long categoryId);
    
    /**
     * 分页获取所有商品（管理端）
     */
    IPage<Product> page(int pageNum, int pageSize, Long categoryId, String keyword, Integer status);
    
    /**
     * 根据ID获取商品
     */
    Product getById(Long id);
    
    /**
     * 创建商品
     */
    void create(Long categoryId, String name, String description, String image, BigDecimal price);
    
    /**
     * 更新商品
     */
    void update(Long id, Long categoryId, String name, String description, String image, BigDecimal price, Integer status);
    
    /**
     * 删除商品
     */
    void delete(Long id);
    
    /**
     * 上架商品
     */
    void onShelf(Long id);
    
    /**
     * 下架商品
     */
    void offShelf(Long id);
}