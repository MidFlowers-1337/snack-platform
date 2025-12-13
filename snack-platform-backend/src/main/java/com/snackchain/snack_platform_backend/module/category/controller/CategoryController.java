package com.snackchain.snack_platform_backend.module.category.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.Category;
import com.snackchain.snack_platform_backend.module.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器（消费者端）
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 获取所有启用的分类列表
     */
    @GetMapping
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.listEnabled();
        return Result.success(categories);
    }
}