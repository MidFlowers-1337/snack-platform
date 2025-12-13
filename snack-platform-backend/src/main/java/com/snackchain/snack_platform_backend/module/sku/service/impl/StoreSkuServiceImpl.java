package com.snackchain.snack_platform_backend.module.sku.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Product;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.entity.StoreSku;
import com.snackchain.snack_platform_backend.mapper.ProductMapper;
import com.snackchain.snack_platform_backend.mapper.StoreMapper;
import com.snackchain.snack_platform_backend.mapper.StoreSkuMapper;
import com.snackchain.snack_platform_backend.module.sku.service.StoreSkuService;
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
 * 门店商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreSkuServiceImpl implements StoreSkuService {
    
    private final StoreSkuMapper storeSkuMapper;
    private final StoreMapper storeMapper;
    private final ProductMapper productMapper;
    
    @Override
    public List<StoreSku> listByStoreId(Long storeId) {
        LambdaQueryWrapper<StoreSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreSku::getStoreId, storeId)
               .eq(StoreSku::getStatus, 1)
               .gt(StoreSku::getStock, 0)  // 只显示有库存的
               .orderByDesc(StoreSku::getCreateTime);
        List<StoreSku> skus = storeSkuMapper.selectList(wrapper);
        
        // 填充商品信息
        fillProductInfo(skus);
        return skus;
    }
    
    @Override
    public List<StoreSku> listByStoreIdAndCategoryId(Long storeId, Long categoryId) {
        // 先获取该分类下的所有商品ID
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getCategoryId, categoryId)
                      .eq(Product::getStatus, 1);
        List<Product> products = productMapper.selectList(productWrapper);
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            return List.of();
        }
        
        // 查询门店SKU
        LambdaQueryWrapper<StoreSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreSku::getStoreId, storeId)
               .in(StoreSku::getProductId, productIds)
               .eq(StoreSku::getStatus, 1)
               .gt(StoreSku::getStock, 0)
               .orderByDesc(StoreSku::getCreateTime);
        List<StoreSku> skus = storeSkuMapper.selectList(wrapper);
        
        // 填充商品信息
        fillProductInfo(skus);
        return skus;
    }
    
    @Override
    public IPage<StoreSku> pageByStoreId(Long storeId, int pageNum, int pageSize, String keyword, Integer status) {
        Page<StoreSku> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StoreSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreSku::getStoreId, storeId);
        
        if (status != null) {
            wrapper.eq(StoreSku::getStatus, status);
        }
        wrapper.orderByDesc(StoreSku::getCreateTime);
        
        IPage<StoreSku> result = storeSkuMapper.selectPage(page, wrapper);
        
        // 填充商品信息
        fillProductInfo(result.getRecords());
        
        // 如果有关键字，需要过滤（因为关键字是针对商品名称的）
        if (StringUtils.hasText(keyword)) {
            List<StoreSku> filtered = result.getRecords().stream()
                    .filter(sku -> sku.getProduct() != null && 
                            sku.getProduct().getName().contains(keyword))
                    .collect(Collectors.toList());
            result.setRecords(filtered);
            result.setTotal(filtered.size());
        }
        
        return result;
    }
    
    @Override
    public StoreSku getById(Long id) {
        StoreSku sku = storeSkuMapper.selectById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }
        
        // 填充商品信息
        if (sku.getProductId() != null) {
            Product product = productMapper.selectById(sku.getProductId());
            sku.setProduct(product);
        }
        return sku;
    }
    
    @Override
    public StoreSku getByStoreIdAndProductId(Long storeId, Long productId) {
        LambdaQueryWrapper<StoreSku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoreSku::getStoreId, storeId)
               .eq(StoreSku::getProductId, productId);
        return storeSkuMapper.selectOne(wrapper);
    }
    
    @Override
    @Transactional
    public void create(Long storeId, Long productId, BigDecimal price, Integer stock) {
        // 验证门店存在
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }
        
        // 验证商品存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        
        // 检查是否已存在
        if (storeSkuMapper.countByStoreIdAndProductId(storeId, productId) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该商品已添加到门店");
        }
        
        StoreSku sku = new StoreSku();
        sku.setStoreId(storeId);
        sku.setProductId(productId);
        sku.setPrice(price != null ? price : product.getPrice());  // 默认使用平台价格
        sku.setStock(stock != null ? stock : 0);
        sku.setStatus(1);  // 默认上架
        
        storeSkuMapper.insert(sku);
        log.info("添加门店商品成功: storeId={}, productId={}", storeId, productId);
    }
    
    @Override
    @Transactional
    public void update(Long id, BigDecimal price, Integer stock, Integer status) {
        StoreSku sku = getById(id);
        
        if (price != null) {
            sku.setPrice(price);
        }
        if (stock != null) {
            sku.setStock(stock);
        }
        if (status != null) {
            sku.setStatus(status);
        }
        
        storeSkuMapper.updateById(sku);
        log.info("更新门店商品成功: {}", id);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        StoreSku sku = getById(id);
        storeSkuMapper.deleteById(id);
        log.info("删除门店商品成功: {}", id);
    }
    
    @Override
    @Transactional
    public void onShelf(Long id) {
        StoreSku sku = getById(id);
        sku.setStatus(1);
        storeSkuMapper.updateById(sku);
        log.info("门店商品上架成功: {}", id);
    }
    
    @Override
    @Transactional
    public void offShelf(Long id) {
        StoreSku sku = getById(id);
        sku.setStatus(0);
        storeSkuMapper.updateById(sku);
        log.info("门店商品下架成功: {}", id);
    }
    
    @Override
    @Transactional
    public boolean deductStock(Long id, Integer quantity) {
        // 使用乐观锁扣减库存
        LambdaUpdateWrapper<StoreSku> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StoreSku::getId, id)
               .ge(StoreSku::getStock, quantity)  // 库存必须大于等于扣减数量
               .setSql("stock = stock - " + quantity);
        
        int rows = storeSkuMapper.update(null, wrapper);
        if (rows > 0) {
            log.info("扣减库存成功: skuId={}, quantity={}", id, quantity);
            return true;
        } else {
            log.warn("扣减库存失败（库存不足）: skuId={}, quantity={}", id, quantity);
            return false;
        }
    }
    
    @Override
    @Transactional
    public void restoreStock(Long id, Integer quantity) {
        LambdaUpdateWrapper<StoreSku> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StoreSku::getId, id)
               .setSql("stock = stock + " + quantity);
        
        storeSkuMapper.update(null, wrapper);
        log.info("恢复库存成功: skuId={}, quantity={}", id, quantity);
    }
    
    /**
     * 填充商品信息
     */
    private void fillProductInfo(List<StoreSku> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        
        // 获取所有商品ID
        List<Long> productIds = skus.stream()
                .map(StoreSku::getProductId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            return;
        }
        
        // 批量查询商品
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 填充商品信息
        for (StoreSku sku : skus) {
            if (sku.getProductId() != null) {
                sku.setProduct(productMap.get(sku.getProductId()));
            }
        }
    }
}