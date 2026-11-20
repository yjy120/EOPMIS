package com.eopmis.controller;

import com.eopmis.common.Result;
import com.eopmis.dto.ChangePasswordRequest;
import com.eopmis.dto.LoginRequest;
import com.eopmis.dto.LoginResponse;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证接口：登录、改密、登出
 *   对应 UC-001、UC-002
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req,
                                      HttpServletRequest request) {
        return Result.ok(authService.login(req, clientIp(request)));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(currentUser.currentUsername(), clientIp(request));
        return Result.ok();
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(
                currentUser.require().getId(),
                req.getOldPassword(),
                req.getNewPassword(),
                req.getConfirmPassword());
        return Result.ok();
    }

    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }
}
