package com.snackchain.snack_platform_backend.module.report.controller;

import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.module.report.service.ReportService;
import com.snackchain.snack_platform_backend.module.report.vo.DashboardVO;
import com.snackchain.snack_platform_backend.module.report.vo.SalesReportVO;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 门店报表控制器
 */
@RestController
@RequestMapping("/store/report")
@RequiredArgsConstructor
@Tag(name = "数据报表-门店")
public class StoreReportController {
    
    private final ReportService reportService;
    
    /**
     * 获取门店仪表盘数据
     */
    @Operation(summary = "获取门店仪表盘数据")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        Long storeId = getCurrentStoreId();
        DashboardVO dashboard = reportService.getStoreDashboard(storeId);
        return Result.success(dashboard);
    }
    
    /**
     * 获取门店销售报表
     */
    @Operation(summary = "获取门店销售报表")
    @GetMapping("/sales")
    public Result<List<SalesReportVO>> salesReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long storeId = getCurrentStoreId();
        List<SalesReportVO> report = reportService.getStoreSalesReport(storeId, startDate, endDate);
        return Result.success(report);
    }
    
    /**
     * 获取当前门店ID
     */
    private Long getCurrentStoreId() {
        Long storeId = UserContextHolder.getStoreId();
        if (storeId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您未绑定门店");
        }
        return storeId;
    }
}