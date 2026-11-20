package com.eopmis.dto;

import lombok.Data;

import java.util.List;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private String token;
    private long expiresIn;
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String roleName;
    private List<String> permissions;
    private List<MenuNode> menus;

    @Data
    public static class MenuNode {
        private Long id;
        private Long parentId;
        private String name;
        private String path;
        private String icon;
        private Integer type;
        private String perm;
        private List<MenuNode> children;
    }
}
