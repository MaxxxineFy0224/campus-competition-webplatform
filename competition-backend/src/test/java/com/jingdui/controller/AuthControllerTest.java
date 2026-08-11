package com.jingdui.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingdui.entity.User;
import com.jingdui.mapper.UserMapper;
import com.jingdui.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试 —— 注册 & 登录
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    /* ================================================================
     * 注册
     * ================================================================ */

    @Test
    @DisplayName("注册成功 —— 返回 JWT token")
    void register_shouldSucceed() throws Exception {
        String name = "新用户";
        String password = "123456";
        String hashedPassword = "$2a$10$hashed";

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(userMapper.insert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return 1;
        });
        when(jwtUtil.generateToken(1L, name)).thenReturn("mock_jwt_token");

        Map<String, String> body = Map.of("name", name, "password", password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock_jwt_token"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.userName").value(name));

        verify(userMapper).selectCount(any());
        verify(passwordEncoder).encode(password);
        verify(userMapper).insert(any(User.class));
        verify(jwtUtil).generateToken(1L, name);
    }

    @Test
    @DisplayName("注册失败 —— 昵称已被占用")
    void register_shouldFail_whenNameDuplicate() throws Exception {
        when(userMapper.selectCount(any())).thenReturn(1L);

        Map<String, String> body = Map.of("name", "已存在用户", "password", "123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("昵称已被占用"));

        verify(userMapper).selectCount(any());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败 —— 昵称为空（参数校验）")
    void register_shouldFail_whenNameEmpty() throws Exception {
        Map<String, String> body = Map.of("name", "", "password", "123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败 —— 密码过短（参数校验）")
    void register_shouldFail_whenPasswordTooShort() throws Exception {
        Map<String, String> body = Map.of("name", "测试用户", "password", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        verify(userMapper, never()).insert(any(User.class));
    }

    /* ================================================================
     * 登录
     * ================================================================ */

    @Test
    @DisplayName("登录成功 —— 返回 JWT token")
    void login_shouldSucceed() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setName("登录用户");
        user.setPasswordHash("$2a$10$hashed");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("123456", "$2a$10$hashed")).thenReturn(true);
        when(jwtUtil.generateToken(2L, "登录用户")).thenReturn("mock_jwt_token");

        Map<String, String> body = Map.of("name", "登录用户", "password", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock_jwt_token"))
                .andExpect(jsonPath("$.data.userId").value(2));
    }

    @Test
    @DisplayName("登录失败 —— 用户不存在")
    void login_shouldFail_whenUserNotFound() throws Exception {
        when(userMapper.selectOne(any())).thenReturn(null);

        Map<String, String> body = Map.of("name", "不存在用户", "password", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("登录失败 —— 密码错误")
    void login_shouldFail_whenPasswordWrong() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setName("登录用户");
        user.setPasswordHash("$2a$10$hashed");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Map<String, String> body = Map.of("name", "登录用户", "password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }
}