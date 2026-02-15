package com.snackchain.snack_platform_backend.module.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.CartItem;
import com.snackchain.snack_platform_backend.entity.Product;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.entity.StoreSku;
import com.snackchain.snack_platform_backend.mapper.CartItemMapper;
import com.snackchain.snack_platform_backend.mapper.ProductMapper;
import com.snackchain.snack_platform_backend.mapper.StoreMapper;
import com.snackchain.snack_platform_backend.mapper.StoreSkuMapper;
import com.snackchain.snack_platform_backend.module.cart.dto.CartItemDTO;
import com.snackchain.snack_platform_backend.module.cart.dto.UpdateCartDTO;
import com.snackchain.snack_platform_backend.module.cart.service.CartService;
import com.snackchain.snack_platform_backend.module.cart.vo.CartItemVO;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final StoreSkuMapper storeSkuMapper;
    private final ProductMapper productMapper;
    private final StoreMapper storeMapper;

    @Override
    public List<CartItemVO> listCartItems() {
        Long userId = getCurrentUserId();

        // 查询用户的购物车项
        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getCreatedAt)
        );

        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询关联的 StoreSku
        List<Long> skuIds = cartItems.stream()
                .map(CartItem::getSkuId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, StoreSku> skuMap = storeSkuMapper.selectBatchIds(skuIds)
                .stream()
                .collect(Collectors.toMap(StoreSku::getId, s -> s));

        // 批量查询关联的 Product
        List<Long> productIds = skuMap.values().stream()
                .map(StoreSku::getProductId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 批量查询关联的 Store
        List<Long> storeIds = cartItems.stream()
                .map(CartItem::getStoreId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Store> storeMap = storeMapper.selectBatchIds(storeIds)
                .stream()
                .collect(Collectors.toMap(Store::getId, s -> s));

        // 组装 VO
        return cartItems.stream().map(item -> {
            StoreSku sku = skuMap.get(item.getSkuId());
            Product product = sku != null ? productMap.get(sku.getProductId()) : null;
            Store store = storeMap.get(item.getStoreId());

            return CartItemVO.builder()
                    .id(item.getId())
                    .storeId(item.getStoreId())
                    .skuId(item.getSkuId())
                    .productName(product != null ? product.getName() : "未知商品")
                    .productImage(product != null ? product.getImage() : null)
                    .price(sku != null ? sku.getPrice() : null)
                    .quantity(item.getQuantity())
                    .stock(sku != null ? sku.getStock() : 0)
                    .storeName(store != null ? store.getName() : "未知门店")
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addItem(CartItemDTO dto) {
        Long userId = getCurrentUserId();

        // 校验 SKU 是否存在且上架
        StoreSku sku = storeSkuMapper.selectById(dto.getSkuId());
        if (sku == null || sku.getStatus() != 1) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }

        // 校验门店ID是否一致
        if (!sku.getStoreId().equals(dto.getStoreId())) {
            throw new BusinessException(ResultCode.SKU_NOT_FOUND);
        }

        // 校验库存
        if (sku.getStock() < dto.getQuantity()) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        // 检查是否已存在同一 SKU 的购物车项
        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSkuId, dto.getSkuId())
        );

        if (existing != null) {
            // 已存在则累加数量
            int newQuantity = existing.getQuantity() + dto.getQuantity();
            if (newQuantity > sku.getStock()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            existing.setQuantity(newQuantity);
            cartItemMapper.updateById(existing);
            log.info("购物车更新: userId={}, skuId={}, quantity={}", userId, dto.getSkuId(), newQuantity);
        } else {
            // 不存在则新建
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setStoreId(dto.getStoreId());
            cartItem.setSkuId(dto.getSkuId());
            cartItem.setQuantity(dto.getQuantity());
            cartItemMapper.insert(cartItem);
            log.info("购物车新增: userId={}, skuId={}, quantity={}", userId, dto.getSkuId(), dto.getQuantity());
        }
    }

    @Override
    @Transactional
    public void updateItem(Long cartItemId, UpdateCartDTO dto) {
        Long userId = getCurrentUserId();

        // 查询购物车项并校验归属
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }

        // 校验库存
        StoreSku sku = storeSkuMapper.selectById(cartItem.getSkuId());
        if (sku != null && dto.getQuantity() > sku.getStock()) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        cartItem.setQuantity(dto.getQuantity());
        cartItemMapper.updateById(cartItem);
        log.info("购物车更新: userId={}, cartItemId={}, quantity={}", userId, cartItemId, dto.getQuantity());
    }

    @Override
    @Transactional
    public void removeItem(Long cartItemId) {
        Long userId = getCurrentUserId();

        // 查询购物车项并校验归属
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }

        cartItemMapper.deleteById(cartItemId);
        log.info("购物车删除: userId={}, cartItemId={}", userId, cartItemId);
    }

    @Override
    @Transactional
    public void clearByStore(Long storeId) {
        Long userId = getCurrentUserId();

        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getStoreId, storeId)
        );
        log.info("购物车清空: userId={}, storeId={}", userId, storeId);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
