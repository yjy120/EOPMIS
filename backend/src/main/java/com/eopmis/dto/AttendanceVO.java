package com.eopmis.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤视图对象（UC-015 HR 端 + UC-017 EMP 端 共用）
 *   冗余 employeeName/deptName/positionName/statusLabel 字段
 */
@Data
public class AttendanceVO {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String empNo;
    private String deptName;
    private String positionName;
    private LocalDate attendanceDate;
    /** 1=出勤 2=迟到 3=早退 4=请假 5=加班 6=缺勤 */
    private Integer status;
    private String statusLabel;
    private String leaveType;
    private BigDecimal overtimeHours;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}