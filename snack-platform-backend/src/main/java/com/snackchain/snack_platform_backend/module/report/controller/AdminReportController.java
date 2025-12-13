package com.snackchain.snack_platform_backend.module.report.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.module.report.service.ReportService;
import com.snackchain.snack_platform_backend.module.report.vo.DashboardVO;
import com.snackchain.snack_platform_backend.module.report.vo.SalesReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 平台报表控制器（系统管理员端）
 */
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class AdminReportController {
    
    private final ReportService reportService;
    
    /**
     * 获取平台仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        DashboardVO dashboard = reportService.getPlatformDashboard();
        return Result.success(dashboard);
    }
    
    /**
     * 获取平台销售报表
     */
    @GetMapping("/sales")
    public Result<List<SalesReportVO>> salesReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<SalesReportVO> report = reportService.getPlatformSalesReport(startDate, endDate);
        return Result.success(report);
    }
    
    /**
     * 获取指定门店的销售报表
     */
    @GetMapping("/store-sales")
    public Result<List<SalesReportVO>> storeSalesReport(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<SalesReportVO> report = reportService.getStoreSalesReport(storeId, startDate, endDate);
        return Result.success(report);
    }
}