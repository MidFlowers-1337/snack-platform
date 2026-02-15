package com.snackchain.snack_platform_backend.module.points.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.PointsRecord;
import com.snackchain.snack_platform_backend.module.points.service.PointsService;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 积分控制器（消费者端）
 *
 * What: 提供积分余额查询、流水记录和积分消费接口
 * Why: 积分系统是用户激励的核心，需要透明的查询与操作入口
 * Why good: 结合 UserContextHolder 保证只能操作自己的积分，安全且简洁
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
@Tag(name = "会员积分")
public class PointsController {

    private final PointsService pointsService;

    @Operation(summary = "查询我的积分余额")
    @GetMapping("/balance")
    public Result<Map<String, Integer>> getBalance() {
        Long userId = UserContextHolder.getUserId();
        int balance = pointsService.getBalance(userId);
        return Result.success(Map.of("balance", balance));
    }

    @Operation(summary = "查询积分流水")
    @GetMapping("/records")
    public Result<IPage<PointsRecord>> getRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContextHolder.getUserId();
        return Result.success(pointsService.getRecords(userId, pageNum, pageSize));
    }

    @Operation(summary = "消费积分")
    @PostMapping("/spend")
    public Result<Void> spendPoints(@RequestBody Map<String, Object> body) {
        Long userId = UserContextHolder.getUserId();
        int points = ((Number) body.get("points")).intValue();
        String description = (String) body.getOrDefault("description", "积分兑换");
        pointsService.spendPoints(userId, points, description);
        return Result.success();
    }
}
