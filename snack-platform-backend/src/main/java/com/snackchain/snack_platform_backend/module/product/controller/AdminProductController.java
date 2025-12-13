package com.snackchain.snack_platform_backend.module.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.Product;
import com.snackchain.snack_platform_backend.module.product.dto.ProductDTO;
import com.snackchain.snack_platform_backend.module.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器（系统管理员端）
 */
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final ProductService productService;
    
    /**
     * 分页获取商品列表
     */
    @GetMapping
    public Result<IPage<Product>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<Product> page = productService.page(pageNum, pageSize, categoryId, keyword, status);
        return Result.success(page);
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }
    
    /**
     * 创建商品
     */
    @PostMapping
    @OperationLog(module = "商品管理", operation = "创建商品")
    public Result<Void> create(@Valid @RequestBody ProductDTO dto) {
        productService.create(dto.getCategoryId(), dto.getName(), dto.getDescription(), 
                dto.getImage(), dto.getPrice());
        return Result.success();
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    @OperationLog(module = "商品管理", operation = "更新商品")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProductDTO dto) {
        productService.update(id, dto.getCategoryId(), dto.getName(), dto.getDescription(),
                dto.getImage(), dto.getPrice(), dto.getStatus());
        return Result.success();
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "商品管理", operation = "删除商品")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
    
    /**
     * 上架商品
     */
    @PutMapping("/{id}/on-shelf")
    public Result<Void> onShelf(@PathVariable Long id) {
        productService.onShelf(id);
        return Result.success();
    }
    
    /**
     * 下架商品
     */
    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(id);
        return Result.success();
    }
}