package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 职工档案 新增 / 编辑 请求体（UC-007 / UC-008）
 *   手机号格式做正则校验，身份证号 / 职工编号由 Service 层校验唯一
 */
@Data
public class EmployeeCreateRequest {

    /** 编辑时必填 */
    private Long id;

    @NotBlank(message = "请填写职工编号")
    private String empNo;

    @NotBlank(message = "请填写姓名")
    private String name;

    @NotNull(message = "请选择性别")
    @Min(value = 1, message = "性别值非法")
    @Max(value = 2, message = "性别值非法")
    private Integer gender;

    private LocalDate birthDate;

    /** 身份证号可空；非空时须为 18 位（末位可为 X），唯一性由 Service 校验 */
    @Pattern(regexp = "^$|^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(message = "请选择部门")
    private Long deptId;

    @NotNull(message = "请选择岗位")
    private Long positionId;

    private String level;

    @NotNull(message = "请选择入职日期")
    private LocalDate entryDate;

    /** 0=离职 1=在职 */
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;

    /** 可选：绑定某个登录账号（普通职工用） */
    private Long userId;

    private String remark;
}