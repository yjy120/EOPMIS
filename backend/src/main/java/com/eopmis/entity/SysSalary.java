package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪资记录（UC-014 / UC-016）
 *   salary_month 格式 YYYY-MM
 *   UNIQUE (employee_id, salary_month) 由 DB 层约束，Service 层补充友好提示
 *   软删除：deleted=0
 */
@Data
@TableName("sys_salary")
public class SysSalary {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 职工 id */
    private Long employeeId;

    /** 薪资月份 YYYY-MM */
    private String salaryMonth;

    /** 基本工资 */
    private BigDecimal baseSalary;

    /** 岗位工资 */
    private BigDecimal positionSalary;

    /** 绩效工资 */
    private BigDecimal performanceSalary;

    /** 补贴 */
    private BigDecimal allowance;

    /** 扣款 */
    private BigDecimal deduction;

    /** 实发工资 = 基本+岗位+绩效+补贴-扣款（由前端计算或 Service 自动计算） */
    private BigDecimal actualSalary;

    /** 发放日期 */
    private LocalDate paymentDate;

    private String remark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}