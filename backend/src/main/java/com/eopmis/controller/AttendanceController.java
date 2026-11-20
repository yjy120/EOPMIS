package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.AttendanceCreateRequest;
import com.eopmis.dto.AttendanceVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

/**
 * UC-015 录入考勤记录（HR 端）
 *   仅人事专员可访问（类级 @PreAuthorize 拦截）
 */
@RestController
@RequestMapping("/hr/attendance")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<AttendanceVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "20") int pageSize,
                                              @RequestParam(required = false) Long employeeId,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Long deptId) {
        return Result.ok(attendanceService.page(pageNo, pageSize, employeeId, startDate, endDate, status, deptId));
    }

    @GetMapping("/{id}")
    public Result<AttendanceVO> detail(@PathVariable Long id) {
        return Result.ok(attendanceService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody AttendanceCreateRequest req) {
        return Result.ok(attendanceService.create(req, currentUser.require().getId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody AttendanceCreateRequest req) {
        attendanceService.update(req, currentUser.require().getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id, currentUser.require().getId());
        return Result.ok();
    }
}