package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.SalaryCreateRequest;
import com.eopmis.dto.SalaryVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * UC-014 登记薪资信息（HR 端）
 *   仅人事专员可访问（类级 @PreAuthorize 拦截）
 */
@RestController
@RequestMapping("/hr/salary")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<SalaryVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                        @RequestParam(defaultValue = "20") int pageSize,
                                        @RequestParam(required = false) Long employeeId,
                                        @RequestParam(required = false) String salaryMonth,
                                        @RequestParam(required = false) Long deptId) {
        return Result.ok(salaryService.page(pageNo, pageSize, employeeId, salaryMonth, deptId));
    }

    @GetMapping("/{id}")
    public Result<SalaryVO> detail(@PathVariable Long id) {
        return Result.ok(salaryService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody SalaryCreateRequest req) {
        return Result.ok(salaryService.create(req, currentUser.require().getId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody SalaryCreateRequest req) {
        salaryService.update(req, currentUser.require().getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        salaryService.delete(id, currentUser.require().getId());
        return Result.ok();
    }
}