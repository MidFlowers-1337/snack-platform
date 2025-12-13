package com.snackchain.snack_platform_backend.module.category.service;

import com.snackchain.snack_platform_backend.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 获取所有启用的分类列表
     */
    List<Category> listEnabled();
    
    /**
     * 获取所有分类列表（管理端）
     */
    List<Category> listAll();
    
    /**
     * 根据ID获取分类
     */
    Category getById(Long id);
    
    /**
     * 创建分类
     */
    void create(String name, String icon, Integer sort);
    
    /**
     * 更新分类
     */
    void update(Long id, String name, String icon, Integer sort, Integer status);
    
    /**
     * 删除分类
     */
    void delete(Long id);
}