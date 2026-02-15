package com.snackchain.snack_platform_backend.module.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.User;
import com.snackchain.snack_platform_backend.enums.UserRole;
import com.snackchain.snack_platform_backend.mapper.UserMapper;
import com.snackchain.snack_platform_backend.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * 用户管理控制器（系统管理员端）
 * 提供用户列表查询、状态管理、角色管理等功能
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理-管理员")
public class AdminUserController {
    
    private final UserMapper userMapper;
    
    /**
     * 分页获取用户列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param username 用户名（可选，模糊搜索）
     * @param role 角色（可选，精确匹配）
     * @return 用户分页列表
     */
    @Operation(summary = "分页获取用户列表")
    @GetMapping
    @RequireRole(UserRole.SYSTEM_ADMIN)
    public Result<IPage<User>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 用户名模糊搜索
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        
        // 角色精确匹配
        if (StringUtils.hasText(role)) {
            try {
                UserRole userRole = UserRole.valueOf(role);
                wrapper.eq(User::getRole, userRole);
            } catch (IllegalArgumentException e) {
                // 忽略无效的角色参数
            }
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> result = userMapper.selectPage(page, wrapper);
        
        // 清除密码字段，避免敏感信息泄露
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }
    
    /**
     * 更新用户状态（启用/禁用）
     * @param id 用户ID
     * @param body 请求体，包含enabled字段
     * @return 操作结果
     */
    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @RequireRole(UserRole.SYSTEM_ADMIN)
    @OperationLog(module = "用户管理", operation = "更新用户状态")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            throw new BusinessException("参数错误：enabled不能为空");
        }
        
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不允许禁用系统管理员
        if (user.getRole() == UserRole.SYSTEM_ADMIN && !enabled) {
            throw new BusinessException("不能禁用系统管理员账户");
        }
        
        user.setStatus(enabled ? 1 : 0);
        userMapper.updateById(user);
        
        return Result.success();
    }
    
    /**
     * 更新用户角色
     * @param id 用户ID
     * @param body 请求体，包含role字段
     * @return 操作结果
     */
    @Operation(summary = "更新用户角色")
    @PutMapping("/{id}/role")
    @RequireRole(UserRole.SYSTEM_ADMIN)
    @OperationLog(module = "用户管理", operation = "更新用户角色")
    public Result<Void> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String roleStr = body.get("role");
        if (!StringUtils.hasText(roleStr)) {
            throw new BusinessException("参数错误：role不能为空");
        }
        
        UserRole newRole;
        try {
            newRole = UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的角色类型");
        }
        
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不允许修改系统管理员的角色
        if (user.getRole() == UserRole.SYSTEM_ADMIN) {
            throw new BusinessException("不能修改系统管理员的角色");
        }
        
        // 如果要设置为门店管理员，需要清除storeId（由门店分配时设置）
        if (newRole == UserRole.STORE_ADMIN) {
            user.setStoreId(null);
        }
        
        // 如果从门店管理员改为其他角色，清除storeId
        if (user.getRole() == UserRole.STORE_ADMIN && newRole != UserRole.STORE_ADMIN) {
            user.setStoreId(null);
        }
        
        user.setRole(newRole);
        userMapper.updateById(user);
        
        return Result.success();
    }
}