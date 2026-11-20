package com.eopmis.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪资视图对象（UC-014 HR 端 + UC-016 EMP 端 共用）
 *   冗余 employeeName/deptName/positionName 字段
 */
@Data
public class SalaryVO {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String empNo;
    private String deptName;
    private String positionName;
    private String salaryMonth;
    private BigDecimal baseSalary;
    private BigDecimal positionSalary;
    private BigDecimal performanceSalary;
    private BigDecimal allowance;
    private BigDecimal deduction;
    private BigDecimal actualSalary;
    private LocalDate paymentDate;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}