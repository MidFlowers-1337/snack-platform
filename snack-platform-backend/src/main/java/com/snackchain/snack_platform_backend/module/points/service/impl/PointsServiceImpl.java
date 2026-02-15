package com.snackchain.snack_platform_backend.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.PointsRecord;
import com.snackchain.snack_platform_backend.entity.User;
import com.snackchain.snack_platform_backend.mapper.PointsRecordMapper;
import com.snackchain.snack_platform_backend.mapper.UserMapper;
import com.snackchain.snack_platform_backend.module.points.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务实现
 *
 * What: 管理用户积分的获得与消耗
 * Why: 积分系统是电商平台用户激励的核心机制
 * Why good: 事务保证积分余额和流水一致性，防止并发问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserMapper userMapper;
    private final PointsRecordMapper pointsRecordMapper;

    @Override
    public int getBalance(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getPoints() != null ? user.getPoints() : 0;
    }

    @Override
    @Transactional
    public void earnFromOrder(Long userId, Long orderId, int amount) {
        if (amount <= 0) return;

        // 每消费1元获得1积分
        int earnPoints = amount;

        // 更新用户积分
        User user = userMapper.selectById(userId);
        if (user == null) return;

        user.setPoints((user.getPoints() != null ? user.getPoints() : 0) + earnPoints);
        userMapper.updateById(user);

        // 记录流水
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(earnPoints);
        record.setType("EARN");
        record.setDescription("订单完成获得积分");
        record.setOrderId(orderId);
        pointsRecordMapper.insert(record);

        log.info("用户 {} 从订单 {} 获得 {} 积分", userId, orderId, earnPoints);
    }

    @Override
    @Transactional
    public boolean spendPoints(Long userId, int points, String description) {
        if (points <= 0) return false;

        User user = userMapper.selectById(userId);
        if (user == null) return false;

        int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        if (currentPoints < points) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "积分不足");
        }

        // 扣减积分
        user.setPoints(currentPoints - points);
        userMapper.updateById(user);

        // 记录流水
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(-points);
        record.setType("SPEND");
        record.setDescription(description);
        pointsRecordMapper.insert(record);

        log.info("用户 {} 消耗 {} 积分: {}", userId, points, description);
        return true;
    }

    @Override
    public IPage<PointsRecord> getRecords(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId)
               .orderByDesc(PointsRecord::getCreateTime);
        return pointsRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
