package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 劳动合同 Mapper（UC-010/011/012）
 *   自定义方法全部使用 @Select 内联 SQL
 *   myContracts 使用 Map 接收联表查询字段
 */
@Mapper
public interface SysContractMapper extends BaseMapper<SysContract> {

    /**
     * 校验合同编号唯一（excludeId 排除自身）
     */
    @Select("SELECT COUNT(*) FROM sys_contract WHERE contract_no = #{contractNo} AND deleted = 0 AND id <> #{excludeId}")
    int countByContractNo(@Param("contractNo") String contractNo, @Param("excludeId") Long excludeId);

    /**
     * 校验同一职工是否存在有效合同（excludeContractId 排除自身，常用于编辑）
     *   有效 = status=1
     */
    @Select("SELECT COUNT(*) FROM sys_contract WHERE employee_id = #{employeeId} AND status = 1 AND deleted = 0 AND id <> #{excludeContractId}")
    int countActiveByEmployeeId(@Param("employeeId") Long employeeId, @Param("excludeContractId") Long excludeContractId);

    /**
     * UC-012 本人合同：根据当前用户 id 反查 sys_user.employee_id 拿到本人合同列表
     *   联 sys_employee + sys_dept + sys_position 输出冗余字段
     */
    @Select("SELECT "
          + "  c.id, c.contract_no, c.employee_id, c.contract_type, c.sign_date, c.start_date, c.end_date, "
          + "  c.probation_months, c.term_months, c.status, c.terminate_date, c.terminate_reason, c.remark, "
          + "  e.name AS employee_name, e.emp_no, d.name AS dept_name, p.name AS position_name "
          + "FROM sys_contract c "
          + "JOIN sys_employee e ON e.id = c.employee_id AND e.deleted = 0 "
          + "LEFT JOIN sys_dept d ON d.id = e.dept_id "
          + "LEFT JOIN sys_position p ON p.id = e.position_id "
          + "WHERE c.deleted = 0 AND c.employee_id = (SELECT employee_id FROM sys_user WHERE id = #{userId} AND deleted = 0) "
          + "ORDER BY c.start_date DESC, c.id DESC")
    List<Map<String, Object>> myContracts(@Param("userId") Long userId);

    /**
     * HR 端分页查询（联表取冗余字段）
     *   注意：分页由 MyBatis-Plus `selectPage` + 自定义 SQL 实现，此处保留扩展空间
     */
    @Select("SELECT "
          + "  c.id, c.contract_no, c.employee_id, c.contract_type, c.sign_date, c.start_date, c.end_date, "
          + "  c.probation_months, c.term_months, c.status, c.terminate_date, c.terminate_reason, c.remark, "
          + "  e.name AS employee_name, e.emp_no, d.name AS dept_name, p.name AS position_name "
          + "FROM sys_contract c "
          + "JOIN sys_employee e ON e.id = c.employee_id AND e.deleted = 0 "
          + "LEFT JOIN sys_dept d ON d.id = e.dept_id "
          + "LEFT JOIN sys_position p ON p.id = e.position_id "
          + "WHERE c.deleted = 0 "
          + "ORDER BY c.start_date DESC, c.id DESC")
    List<Map<String, Object>> selectAllForPage();
}