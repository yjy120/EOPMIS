package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.PositionCreateRequest;
import com.eopmis.dto.PositionVO;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.entity.SysDept;
import com.eopmis.entity.SysPosition;
import com.eopmis.mapper.SysAuditLogMapper;
import com.eopmis.mapper.SysDeptMapper;
import com.eopmis.mapper.SysPositionMapper;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UC-006 岗位管理（仅人事专员 HR）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final SysPositionMapper positionMapper;
    private final SysDeptMapper deptMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;

    @Override
    public IPage<PositionVO> page(int pageNo, int pageSize, Long deptId, String name) {
        Page<SysPosition> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysPosition> qw = new QueryWrapper<>();
        if (deptId != null) qw.eq("dept_id", deptId);
        if (name != null && !name.isBlank()) qw.like("name", name);
        qw.orderByAsc("sort").orderByAsc("id");
        IPage<SysPosition> pg = positionMapper.selectPage(page, qw);
        IPage<PositionVO> voPage = pg.convert(this::toVO);
        fillDeptName(voPage.getRecords());
        return voPage;
    }

    @Override
    public List<PositionVO> listByDept(Long deptId) {
        if (deptId == null) return List.of();
        List<SysPosition> list = positionMapper.selectList(new QueryWrapper<SysPosition>()
                .eq("dept_id", deptId).orderByAsc("sort"));
        List<PositionVO> vos = list.stream().map(this::toVO).collect(Collectors.toList());
        fillDeptName(vos);
        return vos;
    }

    @Override
    public PositionVO detail(Long id) {
        SysPosition p = positionMapper.selectById(id);
        if (p == null) throw new BizException(404, "岗位不存在");
        PositionVO vo = toVO(p);
        fillDeptName(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PositionCreateRequest req, Long currentUserId) {
        SysDept d = deptMapper.selectById(req.getDeptId());
        if (d == null) throw new BizException(404, "所属部门不存在");
        // 3a 同部门同名
        if (positionMapper.countSameNameInDept(req.getDeptId(), req.getName(), 0L) > 0) {
            throw new BizException(40062, "该部门下岗位名称已存在");
        }
        SysPosition p = new SysPosition();
        p.setDeptId(req.getDeptId());
        p.setName(req.getName());
        p.setCode(req.getCode());
        p.setDescription(req.getDescription());
        p.setLevel(req.getLevel());
        p.setSort(req.getSort() == null ? 0 : req.getSort());
        p.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        p.setCreateBy(currentUserId);
        p.setUpdateBy(currentUserId);
        positionMapper.insert(p);
        writeAudit(currentUserId, "POSITION", "CREATE", "新增岗位：" + p.getName() + "（id=" + p.getId() + "）");
        return p.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PositionCreateRequest req, Long currentUserId) {
        if (req.getId() == null) throw new BizException(400, "id 不能为空");
        SysPosition old = positionMapper.selectById(req.getId());
        if (old == null) throw new BizException(404, "岗位不存在");
        SysDept d = deptMapper.selectById(req.getDeptId());
        if (d == null) throw new BizException(404, "所属部门不存在");
        // 3a 同部门同名
        if (positionMapper.countSameNameInDept(req.getDeptId(), req.getName(), req.getId()) > 0) {
            throw new BizException(40062, "该部门下岗位名称已存在");
        }
        old.setDeptId(req.getDeptId());
        old.setName(req.getName());
        old.setCode(req.getCode());
        old.setDescription(req.getDescription());
        old.setLevel(req.getLevel());
        old.setSort(req.getSort() == null ? 0 : req.getSort());
        old.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        positionMapper.updateById(old);
        writeAudit(currentUserId, "POSITION", "UPDATE", "修改岗位：" + old.getName() + "（id=" + old.getId() + "）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        SysPosition p = positionMapper.selectById(id);
        if (p == null) throw new BizException(404, "岗位不存在");
        // 4a 存在在职职工（mapper 当前为占位返回 0，等 employee 表建立后生效）
        if (positionMapper.countActiveEmployees(id) > 0) {
            throw new BizException(40063, "该岗位存在在职职工，无法删除");
        }
        positionMapper.deleteById(id);
        writeAudit(currentUserId, "POSITION", "DELETE", "删除岗位：" + p.getName() + "（id=" + id + "）");
    }

    private PositionVO toVO(SysPosition p) {
        PositionVO vo = new PositionVO();
        BeanUtil.copyProperties(p, vo, "deleted", "createBy", "updateBy");
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

    /** 批量填充 deptName（避免 N+1） */
    private void fillDeptName(List<PositionVO> vos) {
        if (vos == null || vos.isEmpty()) return;
        List<Long> ids = vos.stream().map(PositionVO::getDeptId).distinct().collect(Collectors.toList());
        Map<Long, String> map = new HashMap<>();
        for (SysDept d : deptMapper.selectBatchIds(ids)) {
            map.put(d.getId(), d.getName());
        }
        for (PositionVO vo : vos) {
            vo.setDeptName(map.get(vo.getDeptId()));
        }
    }
}
