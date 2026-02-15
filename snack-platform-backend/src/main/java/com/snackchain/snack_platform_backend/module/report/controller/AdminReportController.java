package com.snackchain.snack_platform_backend.module.report.controller;

import com.alibaba.excel.EasyExcel;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.module.report.service.ReportService;
import com.snackchain.snack_platform_backend.module.report.vo.DashboardVO;
import com.snackchain.snack_platform_backend.module.report.vo.SalesReportExcelVO;
import com.snackchain.snack_platform_backend.module.report.vo.SalesReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * 平台报表控制器（系统管理员端）
 */
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "数据报表-管理员")
public class AdminReportController {
    
    private final ReportService reportService;
    
    /**
     * 获取平台仪表盘数据
     */
    @Operation(summary = "获取平台仪表盘数据")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        DashboardVO dashboard = reportService.getPlatformDashboard();
        return Result.success(dashboard);
    }
    
    /**
     * 获取平台销售报表
     */
    @Operation(summary = "获取平台销售报表")
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
    @Operation(summary = "获取指定门店的销售报表")
    @GetMapping("/store-sales")
    public Result<List<SalesReportVO>> storeSalesReport(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<SalesReportVO> report = reportService.getStoreSalesReport(storeId, startDate, endDate);
        return Result.success(report);
    }

    /**
     * 导出销售报表为 Excel
     */
    @Operation(summary = "导出销售报表Excel")
    @GetMapping("/export")
    @OperationLog(module = "数据报表", operation = "导出销售报表")
    public void exportSalesReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("销售报表_" + startDate + "_" + endDate + ".xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // 查询数据并转换
        List<SalesReportVO> data = reportService.getPlatformSalesReport(startDate, endDate);
        List<SalesReportExcelVO> excelData = data.stream()
                .map(SalesReportExcelVO::from)
                .toList();

        // 流式写入 Excel
        EasyExcel.write(response.getOutputStream(), SalesReportExcelVO.class)
                .sheet("销售报表")
                .doWrite(excelData);

        log.info("导出销售报表: {} ~ {}, 共 {} 条", startDate, endDate, excelData.size());
    }
}