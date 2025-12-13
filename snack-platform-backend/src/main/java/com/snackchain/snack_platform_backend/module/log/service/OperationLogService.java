package com.snackchain.snack_platform_backend.module.log.service;

import com.snackchain.snack_platform_backend.entity.OperationLog;
import com.snackchain.snack_platform_backend.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {
    
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 异步保存操作日志
     */
    @Async
    public void saveLogAsync(Long userId, String username, String module, String operation,
                             String method, String params, String ip, int status, String errorMsg) {
        try {
            OperationLog logEntity = new OperationLog();
            logEntity.setUserId(userId);
            logEntity.setUsername(username);
            logEntity.setModule(module);
            logEntity.setOperation(operation);
            logEntity.setMethod(method);
            logEntity.setParams(params);
            logEntity.setIp(ip);
            logEntity.setStatus(status);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setCreatedAt(LocalDateTime.now());
            
            operationLogMapper.insert(logEntity);
            log.debug("操作日志已保存: module={}, operation={}, user={}", module, operation, username);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}