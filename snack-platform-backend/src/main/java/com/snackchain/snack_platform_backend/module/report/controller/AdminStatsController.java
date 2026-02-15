package com.snackchain.snack_platform_backend.module.report.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.module.report.service.ReportService;
import com.snackchain.snack_platform_backend.module.report.vo.RealtimeStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据大屏统计控制器
 *
 * What: 提供数据大屏页面所需的实时聚合统计数据
 * Why: 将大屏专用接口与日常报表接口分离，职责更清晰
 * Why good: 独立端点便于后续添加缓存策略和权限控制
 */
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@Tag(name = "数据大屏统计")
public class AdminStatsController {

    private final ReportService reportService;

    /**
     * 获取实时统计数据（数据大屏用）
     * 返回内容：今日/昨日订单与销售额、活跃门店、7天趋势、门店排行、最近订单
     */
    @Operation(summary = "获取数据大屏实时统计")
    @GetMapping("/realtime")
    public Result<RealtimeStatsVO> getRealtimeStats() {
        RealtimeStatsVO stats = reportService.getRealtimeStats();
        return Result.success(stats);
    }
}
