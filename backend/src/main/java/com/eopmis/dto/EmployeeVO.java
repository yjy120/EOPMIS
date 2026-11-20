package com.eopmis.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 职工档案视图对象（UC-007/008 HR 端）
 *   含 deptName / positionName 冗余字段，方便前端表格展示
 *   同时也作为 UC-009 本人档案视图（普通职工接口直接返回该 VO）
 */
@Data
public class EmployeeVO {

    private Long id;
    private String empNo;
    private String name;
    private Integer gender;
    /** 1=男 2=女 */
    private String genderLabel;
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private Long deptId;
    private String deptName;
    private Long positionId;
    private String positionName;
    private String level;
    private LocalDate entryDate;
    private Integer status;
    private String statusLabel;
    private Long userId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}