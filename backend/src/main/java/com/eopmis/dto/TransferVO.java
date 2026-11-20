package com.eopmis.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人事调动视图对象（UC-013 HR 端）
 *   冗余 employeeName + from/to deptName/positionName
 */
@Data
public class TransferVO {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String empNo;

    private Long fromDeptId;
    private String fromDeptName;
    private Long fromPositionId;
    private String fromPositionName;

    private Long toDeptId;
    private String toDeptName;
    private Long toPositionId;
    private String toPositionName;

    private LocalDate transferDate;
    private String reason;

    /** 1=已生效 2=已撤销 */
    private Integer status;
    private String statusLabel;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}