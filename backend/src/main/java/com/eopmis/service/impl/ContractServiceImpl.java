package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.ContractCreateRequest;
import com.eopmis.dto.ContractTerminateRequest;
import com.eopmis.dto.ContractVO;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.entity.SysContract;
import com.eopmis.entity.SysDept;
import com.eopmis.entity.SysEmployee;
import com.eopmis.entity.SysPosition;
import com.eopmis.mapper.SysAuditLogMapper;
import com.eopmis.mapper.SysContractMapper;
import com.eopmis.mapper.SysDeptMapper;
import com.eopmis.mapper.SysEmployeeMapper;
import com.eopmis.mapper.SysPositionMapper;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.ContractService;
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
 * 劳动合同服务实现（UC-010 / UC-011 / UC-012）
 *   错误码体系：
 *     40101 合同编号已存在
 *     40102 合同结束日期不能早于开始日期
 *     40103 该职工已存在有效合同，请先处理
 *     40111 新合同结束日期不能早于开始日期（续签）
 *     40112 该合同已被处理，请刷新后再试
 *     40113 终止日期不能早于合同开始日期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final SysContractMapper contractMapper;
    private final SysEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;
    private final SysPositionMapper positionMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;

    @Override
    public IPage<ContractVO> page(int pageNo, int pageSize,
                                  String contractNo, Long employeeId, Integer status) {
        Page<SysContract> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysContract> qw = new QueryWrapper<>();
        if (contractNo != null && !contractNo.isBlank()) qw.like("contract_no", contractNo);
        if (employeeId != null)                          qw.eq("employee_id", employeeId);
        if (status != null)                              qw.eq("status", status);
        qw.orderByDesc("start_date").orderByDesc("id");
        IPage<SysContract> pg = contractMapper.selectPage(page, qw);
        List<ContractVO> vos = pg.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        enrich(vos);
        Page<ContractVO> voPage = new Page<>(pageNo, pageSize, pg.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public ContractVO detail(Long id) {
        SysContract c = contractMapper.selectById(id);
        if (c == null) throw new BizException(404, "合同不存在");
        ContractVO vo = toVO(c);
        enrich(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ContractCreateRequest req, Long currentUserId) {
        // 校验合同编号唯一（excludeId=0）
        if (contractMapper.countByContractNo(req.getContractNo(), 0L) > 0) {
            throw new BizException(40101, "合同编号已存在");
        }
        // 校验职工存在
        SysEmployee emp = employeeMapper.selectById(req.getEmployeeId());
        if (emp == null) {
            throw new BizException(400, "所选职工不存在");
        }
        // 同一职工是否存在有效合同（3c）
        if (contractMapper.countActiveByEmployeeId(req.getEmployeeId(), 0L) > 0) {
            throw new BizException(40103, "该职工已存在有效合同，请先处理");
        }
        SysContract c = new SysContract();
        BeanUtil.copyProperties(req, c, "id", "deleted", "renewFromId");
        c.setStatus(1); // 新建 = 有效
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        c.setCreateBy(currentUserId);
        c.setUpdateBy(currentUserId);
        contractMapper.insert(c);
        writeAudit(currentUserId, "CONTRACT", "CREATE",
                "新增合同：" + c.getContractNo() + "（员工=" + emp.getName() + "，id=" + c.getId() + "）");
        return c.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ContractCreateRequest req, Long currentUserId) {
        if (req.getId() == null) throw new BizException(400, "id 不能为空");
        SysContract old = contractMapper.selectById(req.getId());
        if (old == null) throw new BizException(404, "合同不存在");
        // 已终止的合同不允许再编辑（避免改已锁定状态）
        if (old.getStatus() != null && old.getStatus() == 4) {
            throw new BizException(40112, "该合同已被处理，请刷新后再试");
        }
        // 合同编号唯一（排除自身）
        if (contractMapper.countByContractNo(req.getContractNo(), req.getId()) > 0) {
            throw new BizException(40101, "合同编号已存在");
        }
        SysEmployee emp = employeeMapper.selectById(req.getEmployeeId());
        if (emp == null) {
            throw new BizException(400, "所选职工不存在");
        }
        // 编辑场景下若目标合同不是"已终止"且不是"已到期"，校验该职工其他有效合同不重复
        if (old.getStatus() != null && old.getStatus() == 1) {
            if (contractMapper.countActiveByEmployeeId(req.getEmployeeId(), req.getId()) > 0) {
                throw new BizException(40103, "该职工已存在有效合同，请先处理");
            }
        }
        BeanUtil.copyProperties(req, old, "id", "deleted", "createTime", "createBy", "renewFromId",
                "status", "terminateDate", "terminateReason");
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        contractMapper.updateById(old);
        writeAudit(currentUserId, "CONTRACT", "UPDATE",
                "修改合同：" + old.getContractNo() + "（id=" + old.getId() + "）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        SysContract c = contractMapper.selectById(id);
        if (c == null) throw new BizException(404, "合同不存在");
        contractMapper.deleteById(id);
        writeAudit(currentUserId, "CONTRACT", "DELETE",
                "删除合同：" + c.getContractNo() + "（id=" + id + "）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long renew(Long renewFromId, ContractCreateRequest req, Long currentUserId) {
        // 校验原合同存在且状态=有效（已到期才允许续签，避免重复处理）
        SysContract old = contractMapper.selectById(renewFromId);
        if (old == null) throw new BizException(404, "原合同不存在");
        if (old.getStatus() == null || old.getStatus() != 1) {
            // 3a 异常流
            throw new BizException(40112, "该合同已被处理，请刷新后再试");
        }
        // 新合同日期校验（2a）
        if (req.getEndDate() != null && req.getStartDate() != null
                && req.getEndDate().isBefore(req.getStartDate())) {
            throw new BizException(40111, "结束日期不能早于开始日期");
        }
        // 新合同编号唯一
        if (contractMapper.countByContractNo(req.getContractNo(), 0L) > 0) {
            throw new BizException(40101, "合同编号已存在");
        }
        // 新合同职工必须与原合同一致
        if (!Objects.equals(req.getEmployeeId(), old.getEmployeeId())) {
            throw new BizException(400, "续签合同的职工必须与原合同一致");
        }
        // 写新合同
        SysContract c = new SysContract();
        BeanUtil.copyProperties(req, c, "id", "deleted", "renewFromId");
        c.setStatus(1); // 有效
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        c.setCreateBy(currentUserId);
        c.setUpdateBy(currentUserId);
        contractMapper.insert(c);
        // 把原合同置为"已到期"（status=3）
        old.setStatus(3);
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        contractMapper.updateById(old);
        writeAudit(currentUserId, "CONTRACT", "RENEW",
                "续签合同：" + old.getContractNo() + " → 新合同 " + c.getContractNo() + "（原 id=" + renewFromId + "，新 id=" + c.getId() + "）");
        return c.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long id, ContractTerminateRequest req, Long currentUserId) {
        SysContract c = contractMapper.selectById(id);
        if (c == null) throw new BizException(404, "合同不存在");
        // 已终止/已到期的合同不可再终止
        if (c.getStatus() != null && (c.getStatus() == 3 || c.getStatus() == 4)) {
            throw new BizException(40112, "该合同已被处理，请刷新后再试");
        }
        // 4a 终止日期不能早于合同开始日期
        if (req.getTerminateDate() != null && c.getStartDate() != null
                && req.getTerminateDate().isBefore(c.getStartDate())) {
            throw new BizException(40113, "终止日期不能早于合同开始日期");
        }
        // 终止日期不能晚于今天（合理性约束）
        if (req.getTerminateDate() != null && req.getTerminateDate().isAfter(LocalDate.now())) {
            throw new BizException(400, "终止日期不能晚于今天");
        }
        c.setStatus(4); // 已终止
        c.setTerminateDate(req.getTerminateDate());
        c.setTerminateReason(req.getTerminateReason());
        c.setUpdateTime(LocalDateTime.now());
        c.setUpdateBy(currentUserId);
        contractMapper.updateById(c);
        writeAudit(currentUserId, "CONTRACT", "TERMINATE",
                "终止合同：" + c.getContractNo() + "（id=" + id + "，终止日期=" + req.getTerminateDate() + "）");
    }

    @Override
    public List<ContractVO> myContracts(Long currentUserId) {
        // UC-012 反范式：sys_user.employee_id → 合同列表
        List<Map<String, Object>> rows = contractMapper.myContracts(currentUserId);
        if (rows.isEmpty()) {
            // 2a 异常流（按 PRD 描述："暂无合同信息"），返回空列表，由 Controller 决定 404 vs 200 空数据
            // 约定：若 sys_user.employee_id 为空则 404，否则空数据 200
            return new ArrayList<>();
        }
        List<ContractVO> vos = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            vos.add(mapToVO(row));
        }
        return vos;
    }

    /** Entity → VO（含状态标签） */
    private ContractVO toVO(SysContract c) {
        ContractVO vo = new ContractVO();
        BeanUtil.copyProperties(c, vo, "deleted");
        vo.setStatusLabel(statusLabel(c.getStatus()));
        return vo;
    }

    /** Map 行（来自 sys_contract JOIN） → VO */
    private ContractVO mapToVO(Map<String, Object> row) {
        ContractVO vo = new ContractVO();
        vo.setId(asLong(row.get("id")));
        vo.setContractNo((String) row.get("contract_no"));
        vo.setEmployeeId(asLong(row.get("employee_id")));
        vo.setEmployeeName((String) row.get("employee_name"));
        vo.setEmpNo((String) row.get("emp_no"));
        vo.setDeptName((String) row.get("dept_name"));
        vo.setPositionName((String) row.get("position_name"));
        vo.setContractType((String) row.get("contract_type"));
        vo.setSignDate(asLocalDate(row.get("sign_date")));
        vo.setStartDate(asLocalDate(row.get("start_date")));
        vo.setEndDate(asLocalDate(row.get("end_date")));
        vo.setProbationMonths(asInteger(row.get("probation_months")));
        vo.setTermMonths(asInteger(row.get("term_months")));
        vo.setStatus(asInteger(row.get("status")));
        vo.setStatusLabel(statusLabel(vo.getStatus()));
        vo.setTerminateDate(asLocalDate(row.get("terminate_date")));
        vo.setTerminateReason((String) row.get("terminate_reason"));
        vo.setRemark((String) row.get("remark"));
        return vo;
    }

    /** 批量填充 deptName / positionName / employeeName */
    private void enrich(List<ContractVO> vos) {
        if (vos.isEmpty()) return;
        Set<Long> empIds = vos.stream().map(ContractVO::getEmployeeId)
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
        for (ContractVO vo : vos) {
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
            case 1 -> "有效";
            case 2 -> "即将到期";
            case 3 -> "已到期";
            case 4 -> "已终止";
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

    private static LocalDate asLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate ld) return ld;
        if (o instanceof java.sql.Date sd) return sd.toLocalDate();
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return LocalDate.parse(o.toString());
    }
}