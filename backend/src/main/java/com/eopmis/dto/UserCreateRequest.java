package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理员新增账号
 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 32, message = "账号长度需在 3~32 之间")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotNull(message = "角色不能为空")
    private Long roleId;

    /** 初始密码（可空，为空时使用系统默认） */
    private String initialPassword;

    /** 绑定的员工 ID（普通职工用） */
    private Long employeeId;
}
