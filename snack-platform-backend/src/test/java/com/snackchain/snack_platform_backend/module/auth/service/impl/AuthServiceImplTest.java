package com.snackchain.snack_platform_backend.module.auth.service.impl;

import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.common.util.JwtUtil;
import com.snackchain.snack_platform_backend.entity.User;
import com.snackchain.snack_platform_backend.enums.UserRole;
import com.snackchain.snack_platform_backend.mapper.UserMapper;
import com.snackchain.snack_platform_backend.module.auth.dto.LoginDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.RegisterDTO;
import com.snackchain.snack_platform_backend.module.auth.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 * 
 * 测试目的：验证用户认证相关的核心业务逻辑
 * 测试范围：
 * - 用户注册（正常流程、用户名重复场景）
 * - 用户登录（正常流程、密码错误、账号禁用场景）
 * 
 * @author Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务单元测试")
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // 测试数据
    private User testUser;
    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;

    /**
     * 初始化测试数据
     * 在每个测试方法执行前准备必要的测试对象
     */
    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword"); // BCrypt加密后的密码
        testUser.setNickname("测试用户");
        testUser.setPhone("13800138000");
        testUser.setRole(UserRole.CONSUMER);
        testUser.setStatus(1); // 启用状态

        // 初始化注册DTO
        registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setNickname("新用户");
        registerDTO.setPhone("13900139000");

        // 初始化登录DTO
        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
    }

    /**
     * 测试用例：正常注册
     * 
     * Given: 用户名不存在
     * When: 用户提交注册信息
     * Then: 注册成功，返回Token
     */
    @Test
    @DisplayName("TC-AUTH-001: 正常注册成功")
    void testRegister_Success() {
        // Given: 用户名不存在
        when(userMapper.countByUsername("newuser")).thenReturn(0);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedNewPassword");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L); // 模拟数据库自增ID
            return 1;
        });
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString(), any()))
            .thenReturn("mock.jwt.token");

        // When: 执行注册
        LoginVO result = authService.register(registerDTO);

        // Then: 验证结果
        assertNotNull(result, "注册结果不应为空");
        assertEquals("newuser", result.getUsername(), "用户名应匹配");
        assertEquals("新用户", result.getNickname(), "昵称应匹配");
        assertEquals("CONSUMER", result.getRole(), "角色应为消费者");
        assertNotNull(result.getToken(), "Token不应为空");

        // 验证用户被插入数据库
        verify(userMapper, times(1)).insert(argThat(user -> 
            "newuser".equals(user.getUsername()) &&
            UserRole.CONSUMER.equals(user.getRole()) &&
            user.getStatus() == 1
        ));
    }

    /**
     * 测试用例：用户名重复注册失败
     * 
     * Given: 用户名已存在
     * When: 用户提交注册信息
     * Then: 抛出用户已存在异常
     */
    @Test
    @DisplayName("TC-AUTH-002: 用户名重复时注册失败")
    void testRegister_DuplicateUsername() {
        // Given: 用户名已存在
        when(userMapper.countByUsername("newuser")).thenReturn(1);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.register(registerDTO),
            "应抛出用户已存在异常"
        );

        assertEquals(ResultCode.USER_EXISTS.getCode(), exception.getCode(),
            "错误码应为用户已存在");

        // 验证用户未被插入
        verify(userMapper, never()).insert(any(User.class));
    }

    /**
     * 测试用例：正常登录
     * 
     * Given: 用户存在且密码正确
     * When: 用户提交登录信息
     * Then: 登录成功，返回Token
     */
    @Test
    @DisplayName("TC-AUTH-003: 正常登录返回Token")
    void testLogin_Success() {
        // Given: 用户存在且密码正确
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser", "CONSUMER", null))
            .thenReturn("mock.jwt.token");

        // When: 执行登录
        LoginVO result = authService.login(loginDTO);

        // Then: 验证结果
        assertNotNull(result, "登录结果不应为空");
        assertEquals(1L, result.getId(), "用户ID应匹配");
        assertEquals("testuser", result.getUsername(), "用户名应匹配");
        assertEquals("测试用户", result.getNickname(), "昵称应匹配");
        assertEquals("CONSUMER", result.getRole(), "角色应匹配");
        assertEquals("mock.jwt.token", result.getToken(), "Token应匹配");
    }

    /**
     * 测试用例：密码错误登录失败
     * 
     * Given: 用户存在但密码错误
     * When: 用户提交登录信息
     * Then: 抛出登录错误异常
     */
    @Test
    @DisplayName("TC-AUTH-004: 密码错误时登录失败")
    void testLogin_WrongPassword() {
        // Given: 用户存在但密码错误
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(false);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.login(loginDTO),
            "应抛出登录错误异常"
        );

        assertEquals(ResultCode.USER_LOGIN_ERROR.getCode(), exception.getCode(),
            "错误码应为登录错误");

        // 验证Token未生成
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString(), any());
    }

    /**
     * 测试用例：禁用用户无法登录
     * 
     * Given: 用户存在但账号已禁用
     * When: 用户提交登录信息
     * Then: 抛出账号禁用异常
     */
    @Test
    @DisplayName("TC-AUTH-005: 禁用用户无法登录")
    void testLogin_DisabledUser() {
        // Given: 用户存在但账号已禁用
        testUser.setStatus(0); // 禁用状态
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.login(loginDTO),
            "应抛出账号禁用异常"
        );

        assertEquals(ResultCode.USER_DISABLED.getCode(), exception.getCode(),
            "错误码应为账号禁用");

        // 验证Token未生成
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString(), any());
    }
}