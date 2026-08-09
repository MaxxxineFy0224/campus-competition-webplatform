package com.jingdui.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 获取当前登录用户信息
 *
 * 从拦截器注入的 request attribute 中读取 userId / userName。
 * 未登录时返回 null，由调用方决定是否拒绝访问。
 */
public class CurrentUser {

    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USER_NAME = "currentUserName";

    /**
     * 获取当前用户 ID，未登录返回 null
     */
    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object attr = request.getAttribute(ATTR_USER_ID);
            if (attr instanceof Number num) {
                long id = num.longValue();
                if (id > 0) return id;
            }
        }
        return null;
    }

    /**
     * 获取当前用户名，未登录返回 null
     */
    public static String getUserName() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object attr = request.getAttribute(ATTR_USER_NAME);
            if (attr instanceof String name && !name.isBlank()) {
                return name;
            }
        }
        return null;
    }

    /**
     * 是否已登录
     */
    public static boolean isLogin() {
        return getUserId() != null;
    }

    private static HttpServletRequest getRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
