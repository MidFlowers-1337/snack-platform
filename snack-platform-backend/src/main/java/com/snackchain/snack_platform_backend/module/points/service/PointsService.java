package com.snackchain.snack_platform_backend.module.points.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.entity.PointsRecord;

/**
 * 积分服务接口
 */
public interface PointsService {

    /** 获取用户积分余额 */
    int getBalance(Long userId);

    /** 订单完成后获得积分（每消费1元 = 1积分） */
    void earnFromOrder(Long userId, Long orderId, int amount);

    /** 消费积分（兑换/抵扣） */
    boolean spendPoints(Long userId, int points, String description);

    /** 查询积分流水 */
    IPage<PointsRecord> getRecords(Long userId, int pageNum, int pageSize);
}
