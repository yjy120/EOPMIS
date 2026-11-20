package com.eopmis.dto;

import lombok.Data;

/**
 * 重置密码（管理员操作）
 */
@Data
public class ResetPasswordRequest {
    /** 留空则使用系统默认初始密码 */
    private String newPassword;
}
