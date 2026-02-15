package com.snackchain.snack_platform_backend.module.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Product;
import com.snackchain.snack_platform_backend.entity.Promotion;
import com.snackchain.snack_platform_backend.mapper.ProductMapper;
import com.snackchain.snack_platform_backend.mapper.PromotionMapper;
import com.snackchain.snack_platform_backend.module.promotion.dto.PromotionDTO;
import com.snackchain.snack_platform_backend.module.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 促销活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionMapper promotionMapper;
    private final ProductMapper productMapper;

    @Override
    public Promotion create(PromotionDTO dto) {
        // 校验时间
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "结束时间不能早于开始时间");
        }
        if (dto.getPromotionPrice().compareTo(dto.getOriginalPrice()) >= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "促销价必须低于原价");
        }

        Promotion promotion = new Promotion();
        promotion.setProductId(dto.getProductId());
        promotion.setTitle(dto.getTitle());
        promotion.setOriginalPrice(dto.getOriginalPrice());
        promotion.setPromotionPrice(dto.getPromotionPrice());
        promotion.setStock(dto.getStock());
        promotion.setSoldCount(0);
        promotion.setStartTime(dto.getStartTime());
        promotion.setEndTime(dto.getEndTime());
        promotion.setStatus(1);

        promotionMapper.insert(promotion);
        log.info("创建促销活动: id={}, productId={}, title={}", promotion.getId(), dto.getProductId(), dto.getTitle());
        return promotion;
    }

    @Override
    public void update(Long id, PromotionDTO dto) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "促销活动不存在");
        }

        promotion.setProductId(dto.getProductId());
        promotion.setTitle(dto.getTitle());
        promotion.setOriginalPrice(dto.getOriginalPrice());
        promotion.setPromotionPrice(dto.getPromotionPrice());
        promotion.setStock(dto.getStock());
        promotion.setStartTime(dto.getStartTime());
        promotion.setEndTime(dto.getEndTime());

        promotionMapper.updateById(promotion);
    }

    @Override
    public void delete(Long id) {
        promotionMapper.deleteById(id);
    }

    @Override
    public IPage<Promotion> page(int pageNum, int pageSize) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Promotion::getCreateTime);
        IPage<Promotion> page = promotionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 填充商品信息
        fillProductInfo(page.getRecords());
        return page;
    }

    @Override
    public List<Promotion> listActive() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getStatus, 1)
               .le(Promotion::getStartTime, now)
               .ge(Promotion::getEndTime, now)
               .apply("(stock - sold_count) > 0")
               .orderByAsc(Promotion::getEndTime);
        List<Promotion> list = promotionMapper.selectList(wrapper);

        // 填充商品信息
        fillProductInfo(list);
        return list;
    }

    @Override
    public Promotion getActiveByProductId(Long productId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getProductId, productId)
               .eq(Promotion::getStatus, 1)
               .le(Promotion::getStartTime, now)
               .ge(Promotion::getEndTime, now)
               .apply("(stock - sold_count) > 0")
               .last("LIMIT 1");
        return promotionMapper.selectOne(wrapper);
    }

    @Override
    public boolean deductStock(Long promotionId, int quantity) {
        int rows = promotionMapper.deductStock(promotionId, quantity);
        return rows > 0;
    }

    /**
     * 填充商品名称和图片
     */
    private void fillProductInfo(List<Promotion> promotions) {
        if (promotions == null || promotions.isEmpty()) return;

        List<Long> productIds = promotions.stream()
                .map(Promotion::getProductId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (Promotion p : promotions) {
            Product product = productMap.get(p.getProductId());
            if (product != null) {
                p.setProductName(product.getName());
                p.setProductImage(product.getImage());
            }
        }
    }
}
