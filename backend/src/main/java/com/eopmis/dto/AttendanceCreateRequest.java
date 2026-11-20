package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 考勤记录 新增 / 编辑 请求体（UC-015）
 *   status: 1=出勤 2=迟到 3=早退 4=请假 5=加班 6=缺勤
 *   overtimeHours 仅在 status=5 时使用
 *   leaveType 仅在 status=4 时使用
 */
@Data
public class AttendanceCreateRequest {

    /** 编辑时必填 */
    private Long id;

    @NotNull(message = "请选择职工")
    private Long employeeId;

    @NotNull(message = "请选择考勤日期")
    private LocalDate attendanceDate;

    @NotNull(message = "请选择出勤状态")
    @Min(value = 1, message = "出勤状态取值 1~6")
    @Max(value = 6, message = "出勤状态取值 1~6")
    private Integer status;

    private String leaveType;

    @DecimalMin(value = "0.00", inclusive = true, message = "加班时长不能为负")
    private BigDecimal overtimeHours;

    private String remark;
}