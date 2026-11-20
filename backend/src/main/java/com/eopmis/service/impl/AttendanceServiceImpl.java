package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.AttendanceCreateRequest;
import com.eopmis.dto.AttendanceVO;
import com.eopmis.entity.SysAttendance;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.entity.SysDept;
import com.eopmis.entity.SysEmployee;
import com.eopmis.entity.SysPosition;
import com.eopmis.mapper.SysAttendanceMapper;
import com.eopmis.mapper.SysAuditLogMapper;
import com.eopmis.mapper.SysDeptMapper;
import com.eopmis.mapper.SysEmployeeMapper;
import com.eopmis.mapper.SysPositionMapper;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考勤记录服务实现（UC-015 / UC-017）
 *   错误码体系：
 *     60001 该职工当日已存在考勤记录
 *     60002 考勤日期不能晚于今天
 *     60003 考勤记录不存在
 *     60004 请假时必须填写请假类型
 *     60005 加班时必须填写加班时长
 *     60009 所选职工不存在
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final SysAttendanceMapper attendanceMapper;
    private final SysEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;
    private final SysPositionMapper positionMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;

    @Override
    public IPage<AttendanceVO> page(int pageNo, int pageSize,
                                     Long employeeId, LocalDate startDate, LocalDate endDate,
                                     Integer status, Long deptId) {
        Page<SysAttendance> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysAttendance> qw = new QueryWrapper<>();
        if (employeeId != null) qw.eq("employee_id", employeeId);
        if (startDate != null)   qw.ge("attendance_date", startDate);
        if (endDate != null)     qw.le("attendance_date", endDate);
        if (status != null)      qw.eq("status", status);
        qw.orderByDesc("attendance_date").orderByDesc("id");
        IPage<SysAttendance> pg = attendanceMapper.selectPage(page, qw);
        List<AttendanceVO> vos = pg.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        enrich(vos);
        Page<AttendanceVO> voPage = new Page<>(pageNo, pageSize, pg.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public AttendanceVO detail(Long id) {
        SysAttendance att = attendanceMapper.selectById(id);
        if (att == null) throw new BizException(60003, "考勤记录不存在");
        AttendanceVO vo = toVO(att);
        enrich(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AttendanceCreateRequest req, Long currentUserId) {
        SysEmployee emp = employeeMapper.selectById(req.getEmployeeId());
        if (emp == null) throw new BizException(60009, "所选职工不存在");
        // 3b 考勤日期不能晚于今天
        if (req.getAttendanceDate() != null && req.getAttendanceDate().isAfter(LocalDate.now())) {
            throw new BizException(60002, "考勤日期不能晚于今天");
        }
        // 3a 同一日期已存在记录
        if (attendanceMapper.countByEmployeeDate(req.getEmployeeId(), req.getAttendanceDate(), 0L) > 0) {
            throw new BizException(60001, "该职工当日已存在考勤记录");
        }
        // 业务规则：请假必填 leaveType；加班必填 overtimeHours
        if (req.getStatus() != null && req.getStatus() == 4
                && (req.getLeaveType() == null || req.getLeaveType().isBlank())) {
            throw new BizException(60004, "请假时必须填写请假类型");
        }
        if (req.getStatus() != null && req.getStatus() == 5
                && (req.getOvertimeHours() == null
                || req.getOvertimeHours().compareTo(java.math.BigDecimal.ZERO) <= 0)) {
            throw new BizException(60005, "加班时必须填写加班时长");
        }
        SysAttendance a = new SysAttendance();
        BeanUtil.copyProperties(req, a, "id", "deleted");
        a.setCreateTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        a.setCreateBy(currentUserId);
        a.setUpdateBy(currentUserId);
        attendanceMapper.insert(a);
        writeAudit(currentUserId, "ATTENDANCE", "CREATE",
                "新增考勤：员工=" + emp.getName() + "，日期=" + req.getAttendanceDate() + "，状态=" + req.getStatus() + "，id=" + a.getId());
        return a.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AttendanceCreateRequest req, Long currentUserId) {
        if (req.getId() == null) throw new BizException(400, "id 不能为空");
        SysAttendance old = attendanceMapper.selectById(req.getId());
        if (old == null) throw new BizException(60003, "考勤记录不存在");
        // 同一日期唯一（排除自身）
        if (attendanceMapper.countByEmployeeDate(req.getEmployeeId(), req.getAttendanceDate(), req.getId()) > 0) {
            throw new BizException(60001, "该职工当日已存在考勤记录");
        }
        BeanUtil.copyProperties(req, old, "id", "deleted", "createTime", "createBy");
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        attendanceMapper.updateById(old);
        writeAudit(currentUserId, "ATTENDANCE", "UPDATE",
                "修改考勤：id=" + old.getId() + "，日期=" + req.getAttendanceDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        SysAttendance a = attendanceMapper.selectById(id);
        if (a == null) throw new BizException(60003, "考勤记录不存在");
        attendanceMapper.deleteById(id);
        writeAudit(currentUserId, "ATTENDANCE", "DELETE",
                "删除考勤：id=" + id + "，日期=" + a.getAttendanceDate());
    }

    @Override
    public List<AttendanceVO> myAttendance(Long currentUserId, LocalDate startDate, LocalDate endDate, Integer status) {
        QueryWrapper<SysAttendance> qw = new QueryWrapper<>();
        qw.eq("a.employee_id", "(SELECT employee_id FROM sys_user WHERE id = " + currentUserId + " AND deleted = 0)");
        // myAttendance 通过自定义 @Select 注入，下面用 mapper 方法
        List<Map<String, Object>> rows = attendanceMapper.myAttendance(currentUserId);
        if (rows.isEmpty()) return new ArrayList<>();
        List<AttendanceVO> vos = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            AttendanceVO vo = mapToVO(row);
            // EMP 端按需过滤
            if (startDate != null && vo.getAttendanceDate() != null
                    && vo.getAttendanceDate().isBefore(startDate)) continue;
            if (endDate != null && vo.getAttendanceDate() != null
                    && vo.getAttendanceDate().isAfter(endDate)) continue;
            if (status != null && !Objects.equals(vo.getStatus(), status)) continue;
            vos.add(vo);
        }
        return vos;
    }

    private AttendanceVO toVO(SysAttendance a) {
        AttendanceVO vo = new AttendanceVO();
        BeanUtil.copyProperties(a, vo, "deleted");
        vo.setStatusLabel(statusLabel(a.getStatus()));
        return vo;
    }

    private AttendanceVO mapToVO(Map<String, Object> row) {
        AttendanceVO vo = new AttendanceVO();
        vo.setId(asLong(row.get("id")));
        vo.setEmployeeId(asLong(row.get("employee_id")));
        vo.setEmployeeName((String) row.get("employee_name"));
        vo.setEmpNo((String) row.get("emp_no"));
        vo.setDeptName((String) row.get("dept_name"));
        vo.setPositionName((String) row.get("position_name"));
        vo.setAttendanceDate(asLocalDate(row.get("attendance_date")));
        vo.setStatus(asInteger(row.get("status")));
        vo.setStatusLabel(statusLabel(vo.getStatus()));
        vo.setLeaveType((String) row.get("leave_type"));
        vo.setOvertimeHours(asBigDecimal(row.get("overtime_hours")));
        vo.setRemark((String) row.get("remark"));
        return vo;
    }

    private void enrich(List<AttendanceVO> vos) {
        if (vos.isEmpty()) return;
        Set<Long> empIds = vos.stream().map(AttendanceVO::getEmployeeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysEmployee> empMap = new HashMap<>();
        if (!empIds.isEmpty()) {
            List<SysEmployee> emps = employeeMapper.selectBatchIds(empIds);
            for (SysEmployee e : emps) empMap.put(e.getId(), e);
        }
        Set<Long> deptIds = empMap.values().stream()
                .map(SysEmployee::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> posIds = empMap.values().stream()
                .map(SysEmployee::getPositionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            for (SysDept d : deptMapper.selectBatchIds(deptIds)) deptMap.put(d.getId(), d.getName());
        }
        Map<Long, String> posMap = new HashMap<>();
        if (!posIds.isEmpty()) {
            for (SysPosition p : positionMapper.selectBatchIds(posIds)) posMap.put(p.getId(), p.getName());
        }
        for (AttendanceVO vo : vos) {
            SysEmployee e = empMap.get(vo.getEmployeeId());
            if (e != null) {
                vo.setEmployeeName(e.getName());
                vo.setEmpNo(e.getEmpNo());
                vo.setDeptName(deptMap.get(e.getDeptId()));
                vo.setPositionName(posMap.get(e.getPositionId()));
            }
        }
    }

    private String statusLabel(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 1 -> "出勤";
            case 2 -> "迟到";
            case 3 -> "早退";
            case 4 -> "请假";
            case 5 -> "加班";
            case 6 -> "缺勤";
            default -> "未知";
        };
    }

    private void writeAudit(Long userId, String module, String action, String detail) {
        try {
            SysAuditLog l = new SysAuditLog();
            l.setUserId(userId);
            l.setUsername(currentUser.currentUsername());
            l.setModule(module);
            l.setAction(action);
            l.setDetail(detail);
            l.setResult(1);
            l.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(l);
        } catch (Exception e) {
            log.warn("审计日志写入失败：{}", e.getMessage());
        }
    }

    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        return Long.valueOf(o.toString());
    }

    private static Integer asInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        return Integer.valueOf(o.toString());
    }

    private static java.math.BigDecimal asBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof java.math.BigDecimal bd) return bd;
        if (o instanceof Number n) return new java.math.BigDecimal(n.toString());
        return new java.math.BigDecimal(o.toString());
    }

    private static java.time.LocalDate asLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof java.time.LocalDate ld) return ld;
        if (o instanceof java.sql.Date sd) return sd.toLocalDate();
        return java.time.LocalDate.parse(o.toString());
    }
}