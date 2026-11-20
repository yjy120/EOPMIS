package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 32, message = "账号长度需在 3~32 之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6~64 之间")
    private String password;
}
