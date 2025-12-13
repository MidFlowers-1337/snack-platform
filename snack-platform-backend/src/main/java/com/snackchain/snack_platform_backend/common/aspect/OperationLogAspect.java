package com.snackchain.snack_platform_backend.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.module.log.service.OperationLogService;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 拦截带有@OperationLog注解的方法，记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;
    
    /**
     * 切点：拦截带有@OperationLog注解的方法
     */
    @Pointcut("@annotation(com.snackchain.snack_platform_backend.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }
    
    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLog annotation = signature.getMethod().getAnnotation(OperationLog.class);
        
        // 获取请求信息
        HttpServletRequest request = getRequest();
        String ip = getClientIp(request);
        String method = request != null ? 
                request.getMethod() + " " + request.getRequestURI() : 
                signature.getDeclaringTypeName() + "." + signature.getName();
        
        // 获取用户信息
        Long userId = UserContextHolder.getUserId();
        String username = UserContextHolder.getUsername();
        
        // 获取请求参数
        String params = getParams(joinPoint, signature);
        
        // 执行目标方法
        String errorMsg = null;
        int status = 1; // 默认成功
        
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            status = 0; // 失败
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // 异步保存日志（通过Service调用，确保@Async生效）
            operationLogService.saveLogAsync(
                    userId,
                    username,
                    annotation.module(),
                    annotation.operation(),
                    method,
                    params,
                    ip,
                    status,
                    errorMsg
            );
        }
    }
    
    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 获取请求参数
     */
    private String getParams(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        try {
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            if (paramNames == null || paramNames.length == 0) {
                return null;
            }
            
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                // 过滤敏感参数和不可序列化的对象
                if (arg != null && !isSensitiveParam(paramNames[i]) && isSerializable(arg)) {
                    params.put(paramNames[i], arg);
                }
            }
            
            if (params.isEmpty()) {
                return null;
            }
            
            String json = objectMapper.writeValueAsString(params);
            // 限制参数长度，避免过长
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...";
            }
            return json;
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
            return null;
        }
    }
    
    /**
     * 判断是否为敏感参数
     */
    private boolean isSensitiveParam(String paramName) {
        String lowerName = paramName.toLowerCase();
        return lowerName.contains("password") || 
               lowerName.contains("pwd") || 
               lowerName.contains("secret") ||
               lowerName.contains("token");
    }
    
    /**
     * 判断对象是否可序列化
     */
    private boolean isSerializable(Object obj) {
        if (obj == null) {
            return false;
        }
        // 排除HttpServletRequest、HttpServletResponse等不可序列化的对象
        String className = obj.getClass().getName();
        return !className.contains("HttpServletRequest") &&
               !className.contains("HttpServletResponse") &&
               !className.contains("MultipartFile") &&
               !className.contains("InputStream") &&
               !className.contains("OutputStream");
    }
}