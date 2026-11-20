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
 * 考勤记录（UC-015 / UC-017）
 *   status: 1=出勤 2=迟到 3=早退 4=请假 5=加班 6=缺勤
 *   UNIQUE (employee_id, attendance_date) 由 DB 层约束
 *   软删除：deleted=0
 */
@Data
@TableName("sys_attendance")
public class SysAttendance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 职工 id */
    private Long employeeId;

    /** 考勤日期 */
    private LocalDate attendanceDate;

    /** 1=出勤 2=迟到 3=早退 4=请假 5=加班 6=缺勤 */
    private Integer status;

    /** 请假类型：事假/病假/年假/调休（status=4 时填写） */
    private String leaveType;

    /** 加班时长（小时，status=5 时填写） */
    private BigDecimal overtimeHours;

    private String remark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}