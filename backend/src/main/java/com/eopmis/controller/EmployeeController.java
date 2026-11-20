package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.EmployeeCreateRequest;
import com.eopmis.dto.EmployeeVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * UC-007 录入职工档案 / UC-008 查询与修改
 *   仅人事专员 HR 可访问（类级 @PreAuthorize 拦截）
 */
@RestController
@RequestMapping("/hr/employee")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<EmployeeVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "20") int pageSize,
                                          @RequestParam(required = false) String empNo,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(required = false) Long deptId,
                                          @RequestParam(required = false) Long positionId) {
        return Result.ok(employeeService.page(pageNo, pageSize, empNo, name, deptId, positionId));
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> detail(@PathVariable Long id) {
        return Result.ok(employeeService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody EmployeeCreateRequest req) {
        return Result.ok(employeeService.create(req, currentUser.require().getId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody EmployeeCreateRequest req) {
        employeeService.update(req, currentUser.require().getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id, currentUser.require().getId());
        return Result.ok();
    }
}