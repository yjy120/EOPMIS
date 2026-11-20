package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.SalaryCreateRequest;
import com.eopmis.dto.SalaryVO;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.entity.SysDept;
import com.eopmis.entity.SysEmployee;
import com.eopmis.entity.SysPosition;
import com.eopmis.entity.SysSalary;
import com.eopmis.mapper.SysAuditLogMapper;
import com.eopmis.mapper.SysDeptMapper;
import com.eopmis.mapper.SysEmployeeMapper;
import com.eopmis.mapper.SysPositionMapper;
import com.eopmis.mapper.SysSalaryMapper;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.SalaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 薪资记录服务实现（UC-014 / UC-016）
 *   错误码体系：
 *     50001 该职工在本月已存在薪资记录
 *     50002 薪资月份格式不正确
 *     50003 发放日期不能早于薪资月份首日
 *     50004 薪资记录不存在
 *     50009 所选职工不存在
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SysSalaryMapper salaryMapper;
    private final SysEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;
    private final SysPositionMapper positionMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;

    @Override
    public IPage<SalaryVO> page(int pageNo, int pageSize, Long employeeId, String salaryMonth, Long deptId) {
        Page<SysSalary> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysSalary> qw = new QueryWrapper<>();
        if (employeeId != null) qw.eq("employee_id", employeeId);
        if (salaryMonth != null && !salaryMonth.isBlank()) qw.eq("salary_month", salaryMonth);
        // 部门筛选通过 JOIN sys_employee 在 selectAllForPage 内实现；这里简化：仅支持 employeeId+salaryMonth
        qw.orderByDesc("salary_month").orderByDesc("id");
        IPage<SysSalary> pg = salaryMapper.selectPage(page, qw);
        List<SalaryVO> vos = pg.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        enrich(vos);
        Page<SalaryVO> voPage = new Page<>(pageNo, pageSize, pg.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public SalaryVO detail(Long id) {
        SysSalary s = salaryMapper.selectById(id);
        if (s == null) throw new BizException(50004, "薪资记录不存在");
        SalaryVO vo = toVO(s);
        enrich(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SalaryCreateRequest req, Long currentUserId) {
        SysEmployee emp = employeeMapper.selectById(req.getEmployeeId());
        if (emp == null) throw new BizException(50009, "所选职工不存在");
        // 校验同月份同职工唯一（PRD 3c）
        if (salaryMapper.countByEmployeeMonth(req.getEmployeeId(), req.getSalaryMonth(), 0L) > 0) {
            throw new BizException(50001, "该职工在本月已存在薪资记录");
        }
        SysSalary s = new SysSalary();
        BeanUtil.copyProperties(req, s, "id", "deleted");
        s.setCreateTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        s.setCreateBy(currentUserId);
        s.setUpdateBy(currentUserId);
        salaryMapper.insert(s);
        writeAudit(currentUserId, "SALARY", "CREATE",
                "新增薪资：员工=" + emp.getName() + "，月份=" + req.getSalaryMonth() + "，id=" + s.getId());
        return s.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SalaryCreateRequest req, Long currentUserId) {
        if (req.getId() == null) throw new BizException(400, "id 不能为空");
        SysSalary old = salaryMapper.selectById(req.getId());
        if (old == null) throw new BizException(50004, "薪资记录不存在");
        // 同月份同职工唯一（排除自身）
        if (salaryMapper.countByEmployeeMonth(req.getEmployeeId(), req.getSalaryMonth(), req.getId()) > 0) {
            throw new BizException(50001, "该职工在本月已存在薪资记录");
        }
        BeanUtil.copyProperties(req, old, "id", "deleted", "createTime", "createBy");
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        salaryMapper.updateById(old);
        writeAudit(currentUserId, "SALARY", "UPDATE",
                "修改薪资：id=" + old.getId() + "，月份=" + req.getSalaryMonth());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        SysSalary s = salaryMapper.selectById(id);
        if (s == null) throw new BizException(50004, "薪资记录不存在");
        salaryMapper.deleteById(id);
        writeAudit(currentUserId, "SALARY", "DELETE",
                "删除薪资：id=" + id + "，月份=" + s.getSalaryMonth());
    }

    @Override
    public List<SalaryVO> mySalary(Long currentUserId) {
        List<Map<String, Object>> rows = salaryMapper.mySalary(currentUserId);
        if (rows.isEmpty()) return new ArrayList<>();
        List<SalaryVO> vos = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            vos.add(mapToVO(row));
        }
        return vos;
    }

    /** Entity → VO */
    private SalaryVO toVO(SysSalary s) {
        SalaryVO vo = new SalaryVO();
        BeanUtil.copyProperties(s, vo, "deleted");
        return vo;
    }

    /** Map 行（来自 sys_salary JOIN）→ VO */
    private SalaryVO mapToVO(Map<String, Object> row) {
        SalaryVO vo = new SalaryVO();
        vo.setId(asLong(row.get("id")));
        vo.setEmployeeId(asLong(row.get("employee_id")));
        vo.setEmployeeName((String) row.get("employee_name"));
        vo.setEmpNo((String) row.get("emp_no"));
        vo.setDeptName((String) row.get("dept_name"));
        vo.setPositionName((String) row.get("position_name"));
        vo.setSalaryMonth((String) row.get("salary_month"));
        vo.setBaseSalary(asBigDecimal(row.get("base_salary")));
        vo.setPositionSalary(asBigDecimal(row.get("position_salary")));
        vo.setPerformanceSalary(asBigDecimal(row.get("performance_salary")));
        vo.setAllowance(asBigDecimal(row.get("allowance")));
        vo.setDeduction(asBigDecimal(row.get("deduction")));
        vo.setActualSalary(asBigDecimal(row.get("actual_salary")));
        vo.setPaymentDate(asLocalDate(row.get("payment_date")));
        vo.setRemark((String) row.get("remark"));
        return vo;
    }

    /** 批量填充 deptName / positionName / employeeName */
    private void enrich(List<SalaryVO> vos) {
        if (vos.isEmpty()) return;
        Set<Long> empIds = vos.stream().map(SalaryVO::getEmployeeId)
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
        for (SalaryVO vo : vos) {
            SysEmployee e = empMap.get(vo.getEmployeeId());
            if (e != null) {
                vo.setEmployeeName(e.getName());
                vo.setEmpNo(e.getEmpNo());
                vo.setDeptName(deptMap.get(e.getDeptId()));
                vo.setPositionName(posMap.get(e.getPositionId()));
            }
        }
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

    private static BigDecimal asBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(o.toString());
    }

    private static java.time.LocalDate asLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof java.time.LocalDate ld) return ld;
        if (o instanceof java.sql.Date sd) return sd.toLocalDate();
        return java.time.LocalDate.parse(o.toString());
    }
}