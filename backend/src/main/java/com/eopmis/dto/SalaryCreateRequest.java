package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 薪资记录 新增 / 编辑 请求体（UC-014）
 *   实发工资由前端传入（PRD 3 主成功场景要求 HR 录入完整字段）
 *   金额校验：@DecimalMin("0.00") 避免 3b 异常流 "金额不能为负"
 *   薪资月份校验：YYYY-MM 正则
 */
@Data
public class SalaryCreateRequest {

    /** 编辑时必填 */
    private Long id;

    @NotNull(message = "请选择职工")
    private Long employeeId;

    @NotBlank(message = "请填写薪资月份")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "薪资月份格式为 YYYY-MM")
    private String salaryMonth;

    @NotNull(message = "请填写基本工资")
    @DecimalMin(value = "0.00", inclusive = true, message = "金额不能为负")
    private BigDecimal baseSalary;

    @NotNull(message = "请填写岗位工资")
    @DecimalMin(value = "0.00", inclusive = true, message = "金额不能为负")
    private BigDecimal positionSalary;

    @NotNull(message = "请填写绩效工资")
    @DecimalMin(value = "0.00", inclusive = true, message = "金额不能为负")
    private BigDecimal performanceSalary;

    @NotNull(message = "请填写补贴")
    @DecimalMin(value = "0.00", inclusive = true, message = "金额不能为负")
    private BigDecimal allowance;

    @NotNull(message = "请填写扣款")
    @DecimalMin(value = "0.00", inclusive = true, message = "金额不能为负")
    private BigDecimal deduction;

    @NotNull(message = "请填写实发工资")
    @DecimalMin(value = "0.00", inclusive = true, message = "金额不能为负")
    private BigDecimal actualSalary;

    private LocalDate paymentDate;

    private String remark;
}