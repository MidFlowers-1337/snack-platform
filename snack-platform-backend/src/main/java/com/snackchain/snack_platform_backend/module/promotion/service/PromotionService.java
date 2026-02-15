package com.snackchain.snack_platform_backend.module.promotion.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.entity.Promotion;
import com.snackchain.snack_platform_backend.module.promotion.dto.PromotionDTO;

import java.util.List;

/**
 * 促销活动服务接口
 */
public interface PromotionService {

    /** 创建促销活动 */
    Promotion create(PromotionDTO dto);

    /** 更新促销活动 */
    void update(Long id, PromotionDTO dto);

    /** 删除促销活动 */
    void delete(Long id);

    /** 分页查询（管理端） */
    IPage<Promotion> page(int pageNum, int pageSize);

    /** 获取当前进行中的促销列表（消费者端） */
    List<Promotion> listActive();

    /** 根据商品ID查询进行中的促销 */
    Promotion getActiveByProductId(Long productId);

    /** 扣减促销库存（下单时调用，CAS防超卖） */
    boolean deductStock(Long promotionId, int quantity);
}
