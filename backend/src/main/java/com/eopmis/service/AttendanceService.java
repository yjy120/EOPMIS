package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.AttendanceCreateRequest;
import com.eopmis.dto.AttendanceVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤记录 Service 接口（UC-015 HR 端 + UC-017 EMP 端）
 */
public interface AttendanceService {

    /** 分页查询考勤记录（HR 端，支持 employeeId / startDate / endDate / status / deptId 筛选） */
    IPage<AttendanceVO> page(int pageNo, int pageSize,
                             Long employeeId, LocalDate startDate, LocalDate endDate,
                             Integer status, Long deptId);

    /** 详情 */
    AttendanceVO detail(Long id);

    /** 新增 */
    Long create(AttendanceCreateRequest req, Long currentUserId);

    /** 编辑 */
    void update(AttendanceCreateRequest req, Long currentUserId);

    /** 删除 */
    void delete(Long id, Long currentUserId);

    /** UC-017 本人考勤（按 userId 反查 sys_user.employee_id） */
    List<AttendanceVO> myAttendance(Long currentUserId, LocalDate startDate, LocalDate endDate, Integer status);
}