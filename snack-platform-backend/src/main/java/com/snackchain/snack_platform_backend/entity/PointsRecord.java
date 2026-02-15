package com.snackchain.snack_platform_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水实体
 *
 * What: 记录用户积分的每一次变动（获得/消耗）
 * Why: 支持积分明细查询，便于对账和审计
 * Why good: 流水记录不可篡改，余额可通过流水反算校验
 */
@Data
@TableName("points_records")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 积分变动（正数获得，负数消耗） */
    private Integer points;

    /** 类型：EARN-获得，SPEND-消耗 */
    private String type;

    /** 描述 */
    private String description;

    /** 关联订单ID */
    private Long orderId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
