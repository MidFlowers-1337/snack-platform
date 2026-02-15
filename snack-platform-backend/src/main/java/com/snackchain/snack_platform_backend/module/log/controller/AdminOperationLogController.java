package com.snackchain.snack_platform_backend.module.log.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.OperationLog;
import com.snackchain.snack_platform_backend.enums.UserRole;
import com.snackchain.snack_platform_backend.mapper.OperationLogMapper;
import com.snackchain.snack_platform_backend.security.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 操作日志管理控制器（系统管理员端）
 */
@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
@Tag(name = "操作日志-管理员")
public class AdminOperationLogController {
    
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 分页获取操作日志列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param module 操作模块（可选）
     * @param username 用户名（可选，模糊搜索）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 操作日志分页列表
     */
    @Operation(summary = "分页获取操作日志列表")
    @GetMapping
    @RequireRole(UserRole.SYSTEM_ADMIN)
    public Result<IPage<OperationLog>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 模块精确匹配
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        
        // 用户名模糊搜索
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        
        // 时间范围筛选
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreatedAt, endTime);
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        
        IPage<OperationLog> result = operationLogMapper.selectPage(page, wrapper);
        
        return Result.success(result);
    }
}