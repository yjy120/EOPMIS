package com.eopmis.controller;

import com.eopmis.common.BizException;
import com.eopmis.common.Result;
import com.eopmis.dto.AttendanceVO;
import com.eopmis.dto.ContractVO;
import com.eopmis.dto.EmployeeVO;
import com.eopmis.dto.SalaryVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.AttendanceService;
import com.eopmis.service.ContractService;
import com.eopmis.service.EmployeeService;
import com.eopmis.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * UC-009 本人档案（普通职工）
 *   按 sys_user.employee_id 反查 sys_employee
 *   不校验角色：HR 也可以调用查看自己的档案（如果绑定），未绑定则返回 404
 *   防越权：service 层只允许返回本人绑定的那条，调用方无法传 id
 */
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final EmployeeService employeeService;
    private final ContractService contractService;
    private final SalaryService salaryService;
    private final AttendanceService attendanceService;
    private final CurrentUser currentUser;

    @GetMapping("/profile")
    public Result<EmployeeVO> myProfile() {
        Long uid = currentUser.require().getId();
        return Result.ok(employeeService.myProfile(uid));
    }

    /**
     * UC-012 本人合同（普通职工）
     *   严格按 JWT userId 反查 sys_user.employee_id，禁止传 userId/contractId 入参
     *   2a 异常流：sys_user.employee_id 为空时返回 404 "暂未查询到您的合同信息"
     */
    @GetMapping("/contract")
    public Result<List<ContractVO>> myContracts() {
        Long uid = currentUser.require().getId();
        List<ContractVO> list = contractService.myContracts(uid);
        if (list == null || list.isEmpty()) {
            throw new BizException(404, "暂未查询到您的合同信息");
        }
        return Result.ok(list);
    }

    /**
     * UC-016 本人薪资（普通职工）
     *   2a 默认列出最近 12 个月；3a 未绑定职工返回 404 "暂未查询到您的薪资信息"
     *   4a 防 IDOR：不接 userId / employeeId 入参，按 JWT userId 反查
     */
    @GetMapping("/salary")
    public Result<List<SalaryVO>> mySalary() {
        Long uid = currentUser.require().getId();
        List<SalaryVO> list = salaryService.mySalary(uid);
        if (list == null || list.isEmpty()) {
            throw new BizException(404, "暂未查询到您的薪资信息");
        }
        return Result.ok(list);
    }

    /**
     * UC-017 本人考勤（普通职工）
     *   支持月份区间 + 状态筛选；2a 默认列出最近 1 个月
     *   3a 未绑定职工返回 404 "暂未查询到您的考勤信息"
     *   4a 防 IDOR：不接 userId / employeeId 入参，按 JWT userId 反查
     */
    @GetMapping("/attendance")
    public Result<List<AttendanceVO>> myAttendance(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer status) {
        Long uid = currentUser.require().getId();
        // 默认最近 1 个月
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusMonths(1);
        List<AttendanceVO> list = attendanceService.myAttendance(uid, startDate, endDate, status);
        if (list == null || list.isEmpty()) {
            throw new BizException(404, "暂未查询到您的考勤信息");
        }
        return Result.ok(list);
    }
}