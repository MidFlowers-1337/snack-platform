package com.snackchain.snack_platform_backend.module.auth.service.impl;

import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.common.util.JwtUtil;
import com.snackchain.snack_platform_backend.entity.User;
import com.snackchain.snack_platform_backend.enums.UserRole;
import com.snackchain.snack_platform_backend.mapper.UserMapper;
import com.snackchain.snack_platform_backend.module.auth.dto.ChangePasswordDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.LoginDTO;
import com.snackchain.snack_platform_backend.module.auth.dto.RegisterDTO;
import com.snackchain.snack_platform_backend.module.auth.service.AuthService;
import com.snackchain.snack_platform_backend.module.auth.vo.LoginVO;
import com.snackchain.snack_platform_backend.module.auth.vo.UserVO;
import com.snackchain.snack_platform_backend.security.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Override
    @Transactional
    public LoginVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setRole(UserRole.CONSUMER);  // 注册默认为消费者
        user.setStatus(1);  // 默认启用
        
        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
        
        // 生成Token并返回
        String token = jwtUtil.generateToken(
            user.getId(), 
            user.getUsername(), 
            user.getRole().name(), 
            user.getStoreId()
        );
        
        return LoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .storeId(user.getStoreId())
                .token(token)
                .build();
    }
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_LOGIN_ERROR);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_LOGIN_ERROR);
        }
        
        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        log.info("用户登录成功: {}", user.getUsername());
        
        // 生成Token
        String token = jwtUtil.generateToken(
            user.getId(), 
            user.getUsername(), 
            user.getRole().name(), 
            user.getStoreId()
        );
        
        return LoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .storeId(user.getStoreId())
                .token(token)
                .build();
    }
    
    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .storeId(user.getStoreId())
                .build();
    }
    
    @Override
    @Transactional
    public void updateProfile(String nickname, String phone, String avatar) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        
        // 更新信息
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        
        userMapper.updateById(user);
        log.info("用户信息更新成功: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        // 校验两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH);
        }

        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户密码修改成功: {}", user.getUsername());
    }
}