package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.TransferRequest;
import com.eopmis.dto.TransferVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * UC-013 人事调动（HR 端）
 *   仅人事专员可访问（类级 @PreAuthorize 拦截）
 */
@RestController
@RequestMapping("/hr/transfer")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<TransferVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "20") int pageSize,
                                          @RequestParam(required = false) Long employeeId,
                                          @RequestParam(required = false) Integer status) {
        return Result.ok(transferService.page(pageNo, pageSize, employeeId, status));
    }

    @GetMapping("/{id}")
    public Result<TransferVO> detail(@PathVariable Long id) {
        return Result.ok(transferService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody TransferRequest req) {
        return Result.ok(transferService.create(req, currentUser.require().getId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody TransferRequest req) {
        transferService.update(req, currentUser.require().getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        transferService.delete(id, currentUser.require().getId());
        return Result.ok();
    }

    /**
     * 撤销调动：把 status=2，不动 sys_employee（保留历史）
     */
    @PostMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        transferService.revoke(id, currentUser.require().getId());
        return Result.ok();
    }
}