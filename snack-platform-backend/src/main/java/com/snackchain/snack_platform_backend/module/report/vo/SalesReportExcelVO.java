package com.snackchain.snack_platform_backend.module.report.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销售报表 Excel 导出 VO
 *
 * What: EasyExcel 注解驱动的导出数据模型
 * Why: 后台管理员需要导出报表进行离线分析
 * Why good: 流式写入防 OOM，注解化列映射，中文表头
 */
@Data
@Builder
@HeadRowHeight(25)
public class SalesReportExcelVO {

    @ExcelProperty("日期")
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(15)
    private LocalDate date;

    @ExcelProperty("订单数")
    @ColumnWidth(12)
    private Long orderCount;

    @ExcelProperty("销售额(元)")
    @ColumnWidth(15)
    private BigDecimal sales;

    @ExcelProperty("完成订单数")
    @ColumnWidth(12)
    private Long completedCount;

    @ExcelProperty("取消订单数")
    @ColumnWidth(12)
    private Long cancelledCount;

    /**
     * 从 SalesReportVO 转换
     */
    public static SalesReportExcelVO from(SalesReportVO vo) {
        return SalesReportExcelVO.builder()
                .date(vo.getDate())
                .orderCount(vo.getOrderCount())
                .sales(vo.getSales())
                .completedCount(vo.getCompletedCount())
                .cancelledCount(vo.getCancelledCount())
                .build();
    }
}
