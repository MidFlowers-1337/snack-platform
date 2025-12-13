package com.snackchain.snack_platform_backend.module.report.service;

import com.snackchain.snack_platform_backend.module.report.vo.DashboardVO;
import com.snackchain.snack_platform_backend.module.report.vo.SalesReportVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表服务接口
 */
public interface ReportService {
    
    /**
     * 获取门店仪表盘数据
     */
    DashboardVO getStoreDashboard(Long storeId);
    
    /**
     * 获取平台仪表盘数据（系统管理员）
     */
    DashboardVO getPlatformDashboard();
    
    /**
     * 获取门店销售报表
     */
    List<SalesReportVO> getStoreSalesReport(Long storeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取平台销售报表
     */
    List<SalesReportVO> getPlatformSalesReport(LocalDate startDate, LocalDate endDate);
}