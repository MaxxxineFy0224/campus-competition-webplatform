package com.jingdui.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingdui.common.BusinessException;
import com.jingdui.common.Result;
import com.jingdui.entity.User;
import com.jingdui.mapper.UserMapper;
import com.jingdui.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证 API —— 注册 / 登录 / 修改密码
 */
@Tag(name = "认证", description = "用户注册、登录、修改密码等认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // BCryptPasswordEncoder Bean 定义在 config 中注入
    // AuthController 构造函数由 @RequiredArgsConstructor 自动生成

    @Operation(summary = "注册", description = "新用户注册，昵称需唯一，返回 JWT token")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        // 昵称唯一性
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getName, req.getName()));
        if (exists > 0) {
            throw new BusinessException(400, "昵称已被占用");
        }

        User user = new User();
        user.setName(req.getName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setSchool(req.getSchool());
        user.setMajor(req.getMajor());
        user.setGrade(req.getGrade());
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getName());
        return Result.success("注册成功", Map.of(
                "token", token,
                "userId", user.getId(),
                "userName", user.getName()
        ));
    }

    @Operation(summary = "登录", description = "使用昵称和密码登录，返回 JWT token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getName, req.getName()));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getName());
        return Result.success(Map.of(
                "token", token,
                "userId", user.getId(),
                "userName", user.getName()
        ));
    }

    @Operation(summary = "修改密码", description = "需要登录，验证旧密码后设置新密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                       @RequestAttribute(value = "currentUserId", required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "旧密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
        return Result.success("密码修改成功", null);
    }

    // ---- DTO ----

    @Data
    static class RegisterRequest {
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称不能超过50个字")
        private String name;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度为6-100位")
        private String password;

        private String school;
        private String major;
        private String grade;
    }

    @Data
    static class LoginRequest {
        @NotBlank(message = "昵称不能为空")
        private String name;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    static class ChangePasswordRequest {
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 100, message = "新密码长度为6-100位")
        private String newPassword;
    }
}
