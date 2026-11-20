package com.eopmis.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同视图对象（UC-010 HR 端 + UC-012 EMP 端 共用）
 *   冗余 employeeName/deptName/positionName 字段
 */
@Data
public class ContractVO {

    private Long id;
    private String contractNo;
    private Long employeeId;
    private String employeeName;
    private String empNo;
    private String deptName;
    private String positionName;
    private String contractType;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer probationMonths;
    private Integer termMonths;
    /** 1=有效 2=即将到期 3=已到期 4=已终止 */
    private Integer status;
    private String statusLabel;
    private LocalDate terminateDate;
    private String terminateReason;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}