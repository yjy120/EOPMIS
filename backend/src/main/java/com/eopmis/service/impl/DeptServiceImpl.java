package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.DeptCreateRequest;
import com.eopmis.dto.DeptTreeNode;
import com.eopmis.dto.DeptVO;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.entity.SysDept;
import com.eopmis.mapper.SysAuditLogMapper;
import com.eopmis.mapper.SysDeptMapper;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.DeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UC-005 部门管理（仅人事专员 HR）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final SysDeptMapper deptMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;

    @Override
    public IPage<DeptVO> page(int pageNo, int pageSize, String name, Long parentId) {
        Page<SysDept> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysDept> qw = new QueryWrapper<>();
        if (parentId != null) qw.eq("parent_id", parentId);
        if (name != null && !name.isBlank()) qw.like("name", name);
        qw.orderByAsc("sort").orderByAsc("id");
        IPage<SysDept> pg = deptMapper.selectPage(page, qw);
        return pg.convert(this::toVO);
    }

    @Override
    public List<DeptVO> listAll() {
        List<SysDept> list = deptMapper.selectList(new QueryWrapper<SysDept>()
                .orderByAsc("sort").orderByAsc("id"));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DeptTreeNode> tree() {
        List<SysDept> list = deptMapper.selectList(new QueryWrapper<SysDept>().orderByAsc("sort"));
        Map<Long, DeptTreeNode> map = new LinkedHashMap<>();
        List<DeptTreeNode> roots = new ArrayList<>();
        for (SysDept d : list) {
            DeptTreeNode n = new DeptTreeNode();
            n.setId(d.getId());
            n.setParentId(d.getParentId());
            n.setName(d.getName());
            map.put(d.getId(), n);
        }
        for (SysDept d : list) {
            DeptTreeNode n = map.get(d.getId());
            if (d.getParentId() == null || d.getParentId() == 0L) {
                roots.add(n);
            } else {
                DeptTreeNode p = map.get(d.getParentId());
                if (p != null) p.getChildren().add(n); else roots.add(n);
            }
        }
        return roots;
    }

    @Override
    public DeptVO detail(Long id) {
        SysDept d = deptMapper.selectById(id);
        if (d == null) throw new BizException(404, "部门不存在");
        return toVO(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeptCreateRequest req, Long currentUserId) {
        Long parentId = req.getParentId() == null ? 0L : req.getParentId();
        validateParent(null, parentId);
        // 3a 同级同名
        if (deptMapper.countSiblingSameName(parentId, req.getName(), 0L) > 0) {
            throw new BizException(40052, "同级部门下名称已存在");
        }
        SysDept d = new SysDept();
        d.setParentId(parentId);
        d.setName(req.getName());
        d.setCode(req.getCode());
        d.setLeader(req.getLeader());
        d.setSort(req.getSort() == null ? 0 : req.getSort());
        d.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        d.setCreateTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        d.setCreateBy(currentUserId);
        d.setUpdateBy(currentUserId);
        deptMapper.insert(d);
        writeAudit(currentUserId, "DEPT", "CREATE", "新增部门：" + d.getName() + "（id=" + d.getId() + "）");
        return d.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DeptCreateRequest req, Long currentUserId) {
        if (req.getId() == null) throw new BizException(400, "id 不能为空");
        SysDept old = deptMapper.selectById(req.getId());
        if (old == null) throw new BizException(404, "部门不存在");
        Long parentId = req.getParentId() == null ? 0L : req.getParentId();
        validateParent(req.getId(), parentId);
        // 3a 同级同名
        if (deptMapper.countSiblingSameName(parentId, req.getName(), req.getId()) > 0) {
            throw new BizException(40052, "同级部门下名称已存在");
        }
        old.setParentId(parentId);
        old.setName(req.getName());
        old.setCode(req.getCode());
        old.setLeader(req.getLeader());
        old.setSort(req.getSort() == null ? 0 : req.getSort());
        old.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        deptMapper.updateById(old);
        writeAudit(currentUserId, "DEPT", "UPDATE", "修改部门：" + old.getName() + "（id=" + old.getId() + "）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        SysDept d = deptMapper.selectById(id);
        if (d == null) throw new BizException(404, "部门不存在");
        // 5a 存在下级
        if (deptMapper.countChildren(id) > 0) {
            throw new BizException(40054, "该部门存在下级部门或在职职工，无法删除");
        }
        // 5a 存在在职职工（mapper 当前为占位返回 0，等 employee 表建立后生效）
        if (deptMapper.countActiveEmployees(id) > 0) {
            throw new BizException(40054, "该部门存在下级部门或在职职工，无法删除");
        }
        deptMapper.deleteById(id);
        writeAudit(currentUserId, "DEPT", "DELETE", "删除部门：" + d.getName() + "（id=" + id + "）");
    }

    /**
     * 校验上级部门合法性：
     *   1. parentId 为 null 或 0：合法（顶级）
     *   2. parentId 等于自身：非法
     *   3. 沿 parentId 上溯，若任一祖先 == selfId：非法（不能选自己的下级作为上级）
     */
    private void validateParent(Long selfId, Long parentId) {
        if (parentId == null || parentId == 0L) return;
        if (selfId != null && parentId.equals(selfId)) {
            throw new BizException(40053, "上级部门选择非法");
        }
        Long cur = parentId;
        int guard = 0;
        while (cur != null && cur != 0L) {
            if (selfId != null && cur.equals(selfId)) {
                throw new BizException(40053, "上级部门选择非法");
            }
            cur = deptMapper.selectParentId(cur);
            if (++guard > 1000) {
                // 防御性编程：上溯超过 1000 层视为数据异常，禁止放行
                throw new BizException(40053, "上级部门选择非法");
            }
        }
    }

    private DeptVO toVO(SysDept d) {
        DeptVO vo = new DeptVO();
        BeanUtil.copyProperties(d, vo, "deleted", "createBy", "updateBy");
        return vo;
    }

    /**
     * 写操作审计日志（仿 AuthServiceImpl.writeAudit）
     *   审计写入失败不影响主业务（try-catch 隔离）
     */
    private void writeAudit(Long userId, String module, String action, String detail) {
        try {
            SysAuditLog log = new SysAuditLog();
            log.setUserId(userId);
            log.setUsername(currentUser.currentUsername());
            log.setModule(module);
            log.setAction(action);
            log.setDetail(detail);
            log.setResult(1);
            log.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("审计日志写入失败：{}", e.getMessage());
        }
    }
}
