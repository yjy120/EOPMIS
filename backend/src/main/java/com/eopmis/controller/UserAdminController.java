package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.ResetPasswordRequest;
import com.eopmis.dto.UserCreateRequest;
import com.eopmis.dto.UserVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * UC-003 账号管理接口
 *   仅系统管理员可访问
 */
@RestController
@RequestMapping("/admin/user")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<UserVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                      @RequestParam(defaultValue = "20") int pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) Long roleId) {
        return Result.ok(userAdminService.page(pageNo, pageSize, keyword, status, roleId));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest req) {
        return Result.ok(userAdminService.create(req, currentUser.require().getId()));
    }

    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        userAdminService.enable(id, currentUser.require().getId());
        return Result.ok();
    }

    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        userAdminService.disable(id, currentUser.require().getId());
        return Result.ok();
    }

    @PostMapping("/{id}/resetPassword")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestBody(required = false) ResetPasswordRequest req) {
        if (req == null) req = new ResetPasswordRequest();
        userAdminService.resetPassword(id, req, currentUser.require().getId());
        return Result.ok();
    }
}
