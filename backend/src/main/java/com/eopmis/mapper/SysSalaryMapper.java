package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysSalary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 薪资记录 Mapper（UC-014 / UC-016）
 *   自定义方法全部使用 @Select 内联 SQL
 *   mySalary 使用 Map 接收联表查询字段
 */
@Mapper
public interface SysSalaryMapper extends BaseMapper<SysSalary> {

    /**
     * 校验同月份同职工是否已存在薪资记录（excludeId 排除自身）
     */
    @Select("SELECT COUNT(*) FROM sys_salary WHERE employee_id = #{employeeId} AND salary_month = #{salaryMonth} AND deleted = 0 AND id <> #{excludeId}")
    int countByEmployeeMonth(@Param("employeeId") Long employeeId,
                             @Param("salaryMonth") String salaryMonth,
                             @Param("excludeId") Long excludeId);

    /**
     * UC-016 本人薪资：根据当前用户 id 反查 sys_user.employee_id 拿到本人薪资列表
     */
    @Select("SELECT "
          + "  s.id, s.employee_id, s.salary_month, s.base_salary, s.position_salary, s.performance_salary, "
          + "  s.allowance, s.deduction, s.actual_salary, s.payment_date, s.remark, "
          + "  e.name AS employee_name, e.emp_no, d.name AS dept_name, p.name AS position_name "
          + "FROM sys_salary s "
          + "JOIN sys_employee e ON e.id = s.employee_id AND e.deleted = 0 "
          + "LEFT JOIN sys_dept d ON d.id = e.dept_id "
          + "LEFT JOIN sys_position p ON p.id = e.position_id "
          + "WHERE s.deleted = 0 AND s.employee_id = (SELECT employee_id FROM sys_user WHERE id = #{userId} AND deleted = 0) "
          + "ORDER BY s.salary_month DESC, s.id DESC")
    List<Map<String, Object>> mySalary(@Param("userId") Long userId);
}