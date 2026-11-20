package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.EmployeeCreateRequest;
import com.eopmis.dto.EmployeeVO;

/**
 * 职工档案服务接口（UC-007/008/009）
 */
public interface EmployeeService {

    /**
     * 分页查询职工档案（UC-008）
     */
    IPage<EmployeeVO> page(int pageNo, int pageSize,
                           String empNo, String name,
                           Long deptId, Long positionId);

    /**
     * 查询某条职工详情（UC-008）
     */
    EmployeeVO detail(Long id);

    /**
     * 新增职工（UC-007）
     */
    Long create(EmployeeCreateRequest req, Long currentUserId);

    /**
     * 修改职工（UC-008）
     */
    void update(EmployeeCreateRequest req, Long currentUserId);

    /**
     * 删除职工（UC-008，主成功场景未明确列出但 PRD FR-008 包含）
     */
    void delete(Long id, Long currentUserId);

    /**
     * 当前登录用户对应的本人档案（UC-009）
     *   根据 sys_user.employee_id 反查 sys_employee
     */
    EmployeeVO myProfile(Long currentUserId);
}