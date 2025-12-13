package com.snackchain.snack_platform_backend.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Category;
import com.snackchain.snack_platform_backend.mapper.CategoryMapper;
import com.snackchain.snack_platform_backend.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryMapper categoryMapper;
    
    @Override
    public List<Category> listEnabled() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }
    
    @Override
    public List<Category> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }
    
    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        return category;
    }
    
    @Override
    @Transactional
    public void create(String name, String icon, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setIcon(icon);
        category.setSort(sort != null ? sort : 0);
        category.setStatus(1);
        
        categoryMapper.insert(category);
        log.info("创建分类成功: {}", name);
    }
    
    @Override
    @Transactional
    public void update(Long id, String name, String icon, Integer sort, Integer status) {
        Category category = getById(id);
        
        if (name != null) {
            category.setName(name);
        }
        if (icon != null) {
            category.setIcon(icon);
        }
        if (sort != null) {
            category.setSort(sort);
        }
        if (status != null) {
            category.setStatus(status);
        }
        
        categoryMapper.updateById(category);
        log.info("更新分类成功: {}", id);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        categoryMapper.deleteById(id);
        log.info("删除分类成功: {}", category.getName());
    }
}