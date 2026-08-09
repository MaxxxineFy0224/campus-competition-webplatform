package com.jingdui.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器 —— 从 Authorization Header 提取 JWT，注入 userId
 *
 * 请求头格式：Authorization: Bearer <token>
 * 解析后设置 request attribute 供 CurrentUser 工具类读取。
 *
 * 白名单（无需登录）：/api/auth/**、H2 控制台、GET 只读接口
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    /** 完全放行的路径前缀 */
    private static final String[] WHITELIST = {
            "/api/auth/",
            "/h2-console",
    };

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String path = request.getRequestURI();

        // 白名单完全放行
        for (String wl : WHITELIST) {
            if (path.startsWith(wl)) return true;
        }

        // 提取 Token
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                String userName = jwtUtil.parseToken(token).get("userName", String.class);
                request.setAttribute(CurrentUser.ATTR_USER_ID, userId);
                request.setAttribute(CurrentUser.ATTR_USER_NAME, userName);
            }
        }
        // 未携带 Token 也不阻拦 —— Controller 层通过 CurrentUser.getUserId() 判断
        // 返回 null 表示未登录，写操作接口可以自行拒绝
        return true;
    }
}
