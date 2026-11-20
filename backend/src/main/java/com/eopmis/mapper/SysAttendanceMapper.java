package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 考勤记录 Mapper（UC-015 / UC-017）
 *   自定义方法全部使用 @Select 内联 SQL
 */
@Mapper
public interface SysAttendanceMapper extends BaseMapper<SysAttendance> {

    /**
     * 校验同一天同职工是否已存在考勤记录（excludeId 排除自身）
     */
    @Select("SELECT COUNT(*) FROM sys_attendance WHERE employee_id = #{employeeId} AND attendance_date = #{attendanceDate} AND deleted = 0 AND id <> #{excludeId}")
    int countByEmployeeDate(@Param("employeeId") Long employeeId,
                            @Param("attendanceDate") java.time.LocalDate attendanceDate,
                            @Param("excludeId") Long excludeId);

    /**
     * UC-017 本人考勤：根据当前用户 id 反查 sys_user.employee_id 拿到本人考勤列表
     */
    @Select("SELECT "
          + "  a.id, a.employee_id, a.attendance_date, a.status, a.leave_type, a.overtime_hours, a.remark, "
          + "  e.name AS employee_name, e.emp_no, d.name AS dept_name, p.name AS position_name "
          + "FROM sys_attendance a "
          + "JOIN sys_employee e ON e.id = a.employee_id AND e.deleted = 0 "
          + "LEFT JOIN sys_dept d ON d.id = e.dept_id "
          + "LEFT JOIN sys_position p ON p.id = e.position_id "
          + "WHERE a.deleted = 0 AND a.employee_id = (SELECT employee_id FROM sys_user WHERE id = #{userId} AND deleted = 0) "
          + "ORDER BY a.attendance_date DESC, a.id DESC")
    List<Map<String, Object>> myAttendance(@Param("userId") Long userId);
}