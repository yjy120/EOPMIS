package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.EmployeeCreateRequest;
import com.eopmis.dto.EmployeeVO;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.entity.SysDept;
import com.eopmis.entity.SysEmployee;
import com.eopmis.entity.SysPosition;
import com.eopmis.entity.SysRole;
import com.eopmis.entity.SysUser;
import com.eopmis.mapper.SysAuditLogMapper;
import com.eopmis.mapper.SysDeptMapper;
import com.eopmis.mapper.SysEmployeeMapper;
import com.eopmis.mapper.SysPositionMapper;
import com.eopmis.mapper.SysRoleMapper;
import com.eopmis.mapper.SysUserMapper;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UC-007 录入职工档案 / UC-008 查询与修改 / UC-009 本人档案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final SysEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;
    private final SysPositionMapper positionMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final CurrentUser currentUser;

    @Override
    public IPage<EmployeeVO> page(int pageNo, int pageSize,
                                  String empNo, String name,
                                  Long deptId, Long positionId) {
        Page<SysEmployee> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysEmployee> qw = new QueryWrapper<>();
        if (empNo != null && !empNo.isBlank())     qw.like("emp_no", empNo);
        if (name != null && !name.isBlank())      qw.like("name", name);
        if (deptId != null)                       qw.eq("dept_id", deptId);
        if (positionId != null)                   qw.eq("position_id", positionId);
        qw.orderByDesc("id");
        IPage<SysEmployee> pg = employeeMapper.selectPage(page, qw);
        List<EmployeeVO> vos = pg.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        enrichDeptAndPosition(vos);
        // 直接构造新 Page<EmployeeVO>，避免 pg.convert 覆盖已 enriched 的 records
        Page<EmployeeVO> voPage = new Page<>(pageNo, pageSize, pg.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public EmployeeVO detail(Long id) {
        SysEmployee e = employeeMapper.selectById(id);
        if (e == null) throw new BizException(404, "职工不存在");
        EmployeeVO vo = toVO(e);
        enrichDeptAndPosition(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(EmployeeCreateRequest req, Long currentUserId) {
        validateUnique(req, 0L);
        // 校验部门与岗位存在
        if (deptMapper.selectById(req.getDeptId()) == null) {
            throw new BizException(400, "所属部门不存在");
        }
        if (positionMapper.selectById(req.getPositionId()) == null) {
            throw new BizException(400, "所属岗位不存在");
        }
        validateUserBinding(req.getUserId(), 0L);
        SysEmployee e = new SysEmployee();
        BeanUtil.copyProperties(req, e, "id", "deleted");
        e.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        e.setCreateBy(currentUserId);
        e.setUpdateBy(currentUserId);
        employeeMapper.insert(e);
        // 双向绑定：回写 sys_user.employee_id
        if (e.getUserId() != null) {
            bindUserToEmployee(e.getUserId(), e.getId());
        }
        writeAudit(currentUserId, "EMPLOYEE", "CREATE", "新增职工：" + e.getName() + "（工号=" + e.getEmpNo() + "，id=" + e.getId() + "）");
        return e.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EmployeeCreateRequest req, Long currentUserId) {
        if (req.getId() == null) throw new BizException(400, "id 不能为空");
        SysEmployee old = employeeMapper.selectById(req.getId());
        if (old == null) throw new BizException(404, "职工不存在");
        validateUnique(req, req.getId());
        if (deptMapper.selectById(req.getDeptId()) == null) {
            throw new BizException(400, "所属部门不存在");
        }
        if (positionMapper.selectById(req.getPositionId()) == null) {
            throw new BizException(400, "所属岗位不存在");
        }
        validateUserBinding(req.getUserId(), req.getId());
        Long oldUserId = old.getUserId();
        BeanUtil.copyProperties(req, old, "id", "deleted", "createTime", "createBy");
        old.setUpdateTime(LocalDateTime.now());
        old.setUpdateBy(currentUserId);
        employeeMapper.updateById(old);
        // 双向绑定维护：换绑/解绑/新绑
        Long newUserId = old.getUserId();
        boolean bindingChanged = !java.util.Objects.equals(oldUserId, newUserId);
        if (bindingChanged && oldUserId != null) {
            unbindUser(oldUserId);
        }
        if (bindingChanged && newUserId != null) {
            bindUserToEmployee(newUserId, old.getId());
        }
        writeAudit(currentUserId, "EMPLOYEE", "UPDATE", "修改职工：" + old.getName() + "（id=" + old.getId() + "）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long currentUserId) {
        SysEmployee e = employeeMapper.selectById(id);
        if (e == null) throw new BizException(404, "职工不存在");
        employeeMapper.deleteById(id);
        // 解除登录账号绑定，避免账号悬空指向已删档案
        if (e.getUserId() != null) {
            unbindUser(e.getUserId());
        }
        writeAudit(currentUserId, "EMPLOYEE", "DELETE", "删除职工：" + e.getName() + "（id=" + id + "）");
    }

    @Override
    public EmployeeVO myProfile(Long currentUserId) {
        SysEmployee e = employeeMapper.selectByUserId(currentUserId);
        if (e == null) {
            // 2a 异常流：未找到与该账号绑定的职工档案
            throw new BizException(404, "暂未查询到您的档案信息，请联系人事专员");
        }
        EmployeeVO vo = toVO(e);
        enrichDeptAndPosition(List.of(vo));
        return vo;
    }

    /**
     * 校验 empNo / idCard 唯一性（UC-007 主成功场景 3）
     */
    private void validateUnique(EmployeeCreateRequest req, Long excludeId) {
        if (employeeMapper.countByEmpNo(req.getEmpNo(), excludeId) > 0) {
            // 3b
            throw new BizException(40071, "该职工编号已存在");
        }
        if (req.getIdCard() != null && !req.getIdCard().isBlank()
                && employeeMapper.countByIdCard(req.getIdCard(), excludeId) > 0) {
            // 3a
            throw new BizException(40072, "该身份证号已存在");
        }
    }

    /**
     * 校验登录账号绑定关系：
     *   1. userId 可空（不绑定账号）
     *   2. 账号必须存在
     *   3. 账号角色必须是普通职工（EMPLOYEE），禁止把档案绑到管理员/HR 账号
     *   4. 该账号不能已绑定其他档案
     */
    private void validateUserBinding(Long userId, Long excludeId) {
        if (userId == null) return;
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException(400, "绑定的登录账号不存在");
        }
        SysRole role = roleMapper.selectById(u.getRoleId());
        if (role == null || !"EMPLOYEE".equals(role.getCode())) {
            throw new BizException(400, "仅可绑定普通职工角色的账号");
        }
        if (employeeMapper.countByUserId(userId, excludeId) > 0) {
            throw new BizException(40073, "该登录账号已绑定其他职工档案");
        }
    }

    /** 回写 sys_user.employee_id（双向绑定） */
    private void bindUserToEmployee(Long userId, Long employeeId) {
        SysUser u = userMapper.selectById(userId);
        if (u != null) {
            u.setEmployeeId(employeeId);
            userMapper.updateById(u);
        }
    }

    /** 解除账号的档案绑定（updateById 会忽略 null，必须用 UpdateWrapper 显式置空） */
    private void unbindUser(Long userId) {
        userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getEmployeeId, null));
    }

    /**
     * 批量填充 deptName / positionName 冗余字段
     *   一次查完避免 N+1
     */
    private void enrichDeptAndPosition(List<EmployeeVO> vos) {
        if (vos.isEmpty()) return;
        Set<Long> deptIds = vos.stream().map(EmployeeVO::getDeptId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Set<Long> posIds = vos.stream().map(EmployeeVO::getPositionId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<SysDept> depts = deptMapper.selectBatchIds(deptIds);
            for (SysDept d : depts) deptMap.put(d.getId(), d.getName());
        }
        Map<Long, String> posMap = new HashMap<>();
        if (!posIds.isEmpty()) {
            List<SysPosition> positions = positionMapper.selectBatchIds(posIds);
            for (SysPosition p : positions) posMap.put(p.getId(), p.getName());
        }
        for (EmployeeVO vo : vos) {
            if (vo.getDeptId() != null)     vo.setDeptName(deptMap.get(vo.getDeptId()));
            if (vo.getPositionId() != null) vo.setPositionName(posMap.get(vo.getPositionId()));
        }
    }

    private EmployeeVO toVO(SysEmployee e) {
        EmployeeVO vo = new EmployeeVO();
        BeanUtil.copyProperties(e, vo, "deleted");
        vo.setGenderLabel(e.getGender() != null && e.getGender() == 2 ? "女" : "男");
        vo.setStatusLabel(e.getStatus() != null && e.getStatus() == 0 ? "离职" : "在职");
        return vo;
    }

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