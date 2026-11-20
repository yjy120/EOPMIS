package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.TransferRequest;
import com.eopmis.dto.TransferVO;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.entity.SysDept;
import com.eopmis.entity.SysEmployee;
import com.eopmis.entity.SysPosition;
import com.eopmis.entity.SysTransfer;
import com.eopmis.mapper.SysAuditLogMapper;
import com.eopmis.mapper.SysDeptMapper;
import com.eopmis.mapper.SysEmployeeMapper;
import com.eopmis.mapper.SysPositionMapper;
import com.eopmis.mapper.SysTransferMapper;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 人事调动服务实现（UC-013）
 *   错误码：
 *     40201 调入部门或岗位不存在
 *     40202 调动日期不能早于入职日期
 *     40203 调动保存失败（事务回滚兜底）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final SysTransferMapper transferMapper;
    private final SysEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;
    private final SysPositionMapper positionMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;

    @Override
    public IPage<TransferVO> page(int pageNo, int pageSize, Long employeeId, Integer status) {
        Page<SysTransfer> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysTransfer> qw = new QueryWrapper<>();
        if (employeeId != null) qw.eq("employee_id", employeeId);
        if (status != null)     qw.eq("status", status);
        qw.orderByDesc("transfer_date").orderByDesc("id");
        IPage<SysTransfer> pg = transferMapper.selectPage(page, qw);
        List<TransferVO> vos = pg.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        enrich(vos);
        Page<TransferVO> voPage = new Page<>(pageNo, pageSize, pg.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public TransferVO detail(Long id) {
        SysTransfer t = transferMapper.selectById(id);
        if (t == null) throw new BizException(404, "调动记录不存在");
        TransferVO vo = toVO(t);
        enrich(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TransferRequest req, Long currentUserId) {
        // 校验职工存在
        SysEmployee emp = employeeMapper.selectById(req.getEmployeeId());
        if (emp == null) {
            throw new BizException(400, "所选职工不存在");
        }
        // 校验调入部门和岗位存在（3a）
        SysDept toDept = deptMapper.selectById(req.getToDeptId());
        if (toDept == null) {
            throw new BizException(40201, "调入部门不存在");
        }
        SysPosition toPos = positionMapper.selectById(req.getToPositionId());
        if (toPos == null) {
            throw new BizException(40201, "调入岗位不存在");
        }
        // 校验调出部门和岗位存在
        if (deptMapper.selectById(req.getFromDeptId()) == null) {
            throw new BizException(400, "调出部门不存在");
        }
        if (positionMapper.selectById(req.getFromPositionId()) == null) {
            throw new BizException(400, "调出岗位不存在");
        }
        // 校验调动日期≥入职日期（3b）
        if (emp.getEntryDate() != null && req.getTransferDate() != null
                && req.getTransferDate().isBefore(emp.getEntryDate())) {
            throw new BizException(40202, "调动日期不能早于入职日期");
        }
        // 写调动记录
        SysTransfer t = new SysTransfer();
        BeanUtil.copyProperties(req, t, "id", "deleted");
        t.setStatus(1); // 已生效
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        t.setCreateBy(currentUserId);
        t.setUpdateBy(currentUserId);
        transferMapper.insert(t);
        // 同步更新职工档案：deptId + positionId
        SysEmployee fresh = employeeMapper.selectById(req.getEmployeeId());
        fresh.setDeptId(req.getToDeptId());
        fresh.setPositionId(req.getToPositionId());
        fresh.setUpdateTime(LocalDateTime.now());
        fresh.setUpdateBy(currentUserId);
        employeeMapper.updateById(fresh);
        writeAudit(currentUserId, "TRANSFER", "CREATE",
                "发起调动：员工=" + emp.getName() + "，从部门 id=" + req.getFromDeptId() + " → 部门 id=" + req.getToDeptId()
                        + "，岗位 " + req.getFromPositionId() + " → " + req.getToPositionId() + "（调动 id=" + t.getId() + "）");
        return t.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TransferRequest req, Long currentUserId) {
        if (req.getId() == null) throw new BizException(400, "id 不能为空");
        SysTransfer old = transferMapper.selectById(req.getId());
        if (old == null) throw new BizException(404, "调动记录不存在");
        if (old.getStatus() != null && old.getStatus() == 2) {
            throw new BizException(400, "已撤销的调动不可编辑");
        }
        // 同 create 关键校验
        SysEmployee emp = employeeMapper.selectById(req.getEmployeeId());
        if (emp == null) throw new BizException(400, "所选职工不存在");
        if (deptMapper.selectById(req.getToDeptId()) == null) {
            throw new BizException(40201, "调入部门不存在");
        }
        if (positionMapper.selectById(req.getToPositionId()) == null) {
            throw new BizException(40201, "调入岗位不存在");
        }
        if (emp.getEntryDate() != null && req.getTransferDate() != null
                && req.getTransferDate().isBefore(emp.getEntryDate())) {
            throw new BizException(40202, "调动日期不能早于入职日期");
        }
        BeanUtil.copyProperties(req, old, "id", "deleted", "createTime", "createBy", "status");
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        transferMapper.updateById(old);
        writeAudit(currentUserId, "TRANSFER", "UPDATE",
                "修改调动：id=" + req.getId() + "，员工=" + emp.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        SysTransfer t = transferMapper.selectById(id);
        if (t == null) throw new BizException(404, "调动记录不存在");
        transferMapper.deleteById(id);
        // 注意：删除调动记录时不同步回滚 sys_employee（数据安全，避免历史追溯断裂）
        writeAudit(currentUserId, "TRANSFER", "DELETE", "删除调动：id=" + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id, Long currentUserId) {
        SysTransfer t = transferMapper.selectById(id);
        if (t == null) throw new BizException(404, "调动记录不存在");
        if (t.getStatus() != null && t.getStatus() == 2) {
            throw new BizException(400, "该调动已撤销，无需重复操作");
        }
        t.setStatus(2); // 已撤销
        t.setUpdateTime(LocalDateTime.now());
        t.setUpdateBy(currentUserId);
        transferMapper.updateById(t);
        writeAudit(currentUserId, "TRANSFER", "REVOKE", "撤销调动：id=" + id);
    }

    private TransferVO toVO(SysTransfer t) {
        TransferVO vo = new TransferVO();
        BeanUtil.copyProperties(t, vo, "deleted");
        vo.setStatusLabel(t.getStatus() != null && t.getStatus() == 2 ? "已撤销" : "已生效");
        return vo;
    }

    private void enrich(List<TransferVO> vos) {
        if (vos.isEmpty()) return;
        Set<Long> empIds = vos.stream().map(TransferVO::getEmployeeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysEmployee> empMap = new HashMap<>();
        if (!empIds.isEmpty()) {
            for (SysEmployee e : employeeMapper.selectBatchIds(empIds)) empMap.put(e.getId(), e);
        }
        Set<Long> deptIds = new HashSet<>();
        Set<Long> posIds = new HashSet<>();
        for (TransferVO vo : vos) {
            if (vo.getFromDeptId() != null)     deptIds.add(vo.getFromDeptId());
            if (vo.getToDeptId() != null)       deptIds.add(vo.getToDeptId());
            if (vo.getFromPositionId() != null) posIds.add(vo.getFromPositionId());
            if (vo.getToPositionId() != null)   posIds.add(vo.getToPositionId());
        }
        Map<Long, String> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            for (SysDept d : deptMapper.selectBatchIds(deptIds)) deptMap.put(d.getId(), d.getName());
        }
        Map<Long, String> posMap = new HashMap<>();
        if (!posIds.isEmpty()) {
            for (SysPosition p : positionMapper.selectBatchIds(posIds)) posMap.put(p.getId(), p.getName());
        }
        for (TransferVO vo : vos) {
            SysEmployee e = empMap.get(vo.getEmployeeId());
            if (e != null) {
                vo.setEmployeeName(e.getName());
                vo.setEmpNo(e.getEmpNo());
            }
            vo.setFromDeptName(deptMap.get(vo.getFromDeptId()));
            vo.setFromPositionName(posMap.get(vo.getFromPositionId()));
            vo.setToDeptName(deptMap.get(vo.getToDeptId()));
            vo.setToPositionName(posMap.get(vo.getToPositionId()));
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
}