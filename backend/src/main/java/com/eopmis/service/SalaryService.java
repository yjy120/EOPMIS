package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.SalaryCreateRequest;
import com.eopmis.dto.SalaryVO;

import java.util.List;

/**
 * 薪资记录 Service 接口（UC-014 HR 端 + UC-016 EMP 端）
 */
public interface SalaryService {

    /** 分页查询薪资记录（HR 端，支持 employeeId / salaryMonth / departmentId 筛选） */
    IPage<SalaryVO> page(int pageNo, int pageSize, Long employeeId, String salaryMonth, Long deptId);

    /** 详情 */
    SalaryVO detail(Long id);

    /** 新增 */
    Long create(SalaryCreateRequest req, Long currentUserId);

    /** 编辑 */
    void update(SalaryCreateRequest req, Long currentUserId);

    /** 删除 */
    void delete(Long id, Long currentUserId);

    /** UC-016 本人薪资（按 userId 反查 sys_user.employee_id） */
    List<SalaryVO> mySalary(Long currentUserId);
}