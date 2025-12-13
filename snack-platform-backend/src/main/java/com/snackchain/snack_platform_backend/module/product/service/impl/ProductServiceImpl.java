package com.snackchain.snack_platform_backend.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Category;
import com.snackchain.snack_platform_backend.entity.Product;
import com.snackchain.snack_platform_backend.mapper.CategoryMapper;
import com.snackchain.snack_platform_backend.mapper.ProductMapper;
import com.snackchain.snack_platform_backend.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    
    @Override
    public List<Product> listOnShelf() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .orderByDesc(Product::getCreateTime);
        List<Product> products = productMapper.selectList(wrapper);
        
        // 填充分类信息
        fillCategoryInfo(products);
        return products;
    }
    
    @Override
    public List<Product> listByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
               .eq(Product::getStatus, 1)
               .orderByDesc(Product::getCreateTime);
        List<Product> products = productMapper.selectList(wrapper);
        
        // 填充分类信息
        fillCategoryInfo(products);
        return products;
    }
    
    @Override
    public IPage<Product> page(int pageNum, int pageSize, Long categoryId, String keyword, Integer status) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        
        IPage<Product> result = productMapper.selectPage(page, wrapper);
        
        // 填充分类信息
        fillCategoryInfo(result.getRecords());
        return result;
    }
    
    @Override
    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        
        // 填充分类信息
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            product.setCategory(category);
        }
        return product;
    }
    
    @Override
    @Transactional
    public void create(Long categoryId, String name, String description, String image, BigDecimal price) {
        // 验证分类存在
        if (categoryId != null) {
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
            }
        }
        
        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription(description);
        product.setImage(image);
        product.setPrice(price);
        product.setStatus(1);  // 默认上架
        
        productMapper.insert(product);
        log.info("创建商品成功: {}", name);
    }
    
    @Override
    @Transactional
    public void update(Long id, Long categoryId, String name, String description, String image, BigDecimal price, Integer status) {
        Product product = getById(id);
        
        // 验证分类存在
        if (categoryId != null) {
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
            }
            product.setCategoryId(categoryId);
        }
        
        if (name != null) {
            product.setName(name);
        }
        if (description != null) {
            product.setDescription(description);
        }
        if (image != null) {
            product.setImage(image);
        }
        if (price != null) {
            product.setPrice(price);
        }
        if (status != null) {
            product.setStatus(status);
        }
        
        productMapper.updateById(product);
        log.info("更新商品成功: {}", id);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        productMapper.deleteById(id);
        log.info("删除商品成功: {}", product.getName());
    }
    
    @Override
    @Transactional
    public void onShelf(Long id) {
        Product product = getById(id);
        product.setStatus(1);
        productMapper.updateById(product);
        log.info("商品上架成功: {}", product.getName());
    }
    
    @Override
    @Transactional
    public void offShelf(Long id) {
        Product product = getById(id);
        product.setStatus(0);
        productMapper.updateById(product);
        log.info("商品下架成功: {}", product.getName());
    }
    
    /**
     * 填充分类信息
     */
    private void fillCategoryInfo(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        
        // 获取所有分类ID
        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (categoryIds.isEmpty()) {
            return;
        }
        
        // 批量查询分类
        List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
        
        // 填充分类信息
        for (Product product : products) {
            if (product.getCategoryId() != null) {
                product.setCategory(categoryMap.get(product.getCategoryId()));
            }
        }
    }
}