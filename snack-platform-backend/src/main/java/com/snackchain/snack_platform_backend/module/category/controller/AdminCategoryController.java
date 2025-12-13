package com.snackchain.snack_platform_backend.module.category.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.Category;
import com.snackchain.snack_platform_backend.module.category.dto.CategoryDTO;
import com.snackchain.snack_platform_backend.module.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器（系统管理员端）
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 获取所有分类列表
     */
    @GetMapping
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.listAll();
        return Result.success(categories);
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryDTO dto) {
        categoryService.create(dto.getName(), dto.getIcon(), dto.getSort());
        return Result.success();
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto.getName(), dto.getIcon(), dto.getSort(), dto.getStatus());
        return Result.success();
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}