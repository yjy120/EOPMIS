package com.eopmis.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号 VO（列表）
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private Integer status;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Long employeeId;
    private Integer failCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createTime;
}
