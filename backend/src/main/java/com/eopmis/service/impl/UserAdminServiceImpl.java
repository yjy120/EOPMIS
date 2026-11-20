package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.BizException;
import com.eopmis.dto.ResetPasswordRequest;
import com.eopmis.dto.UserCreateRequest;
import com.eopmis.dto.UserVO;
import com.eopmis.entity.SysRole;
import com.eopmis.entity.SysUser;
import com.eopmis.mapper.SysRoleMapper;
import com.eopmis.mapper.SysUserMapper;
import com.eopmis.service.UserAdminService;
import com.eopmis.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UC-003 账号管理（仅系统管理员）
 *   新增、列表、启用/停用、重置密码
 */
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Value("${password.initial}")
    private String initialPwd;

    @Override
    public IPage<UserVO> page(int pageNo, int pageSize, String keyword, Integer status, Long roleId) {
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        if (status != null) qw.eq("status", status);
        if (roleId != null) qw.eq("role_id", roleId);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("username", keyword)
                    .or().like("real_name", keyword));
        }
        qw.orderByDesc("create_time");
        IPage<SysUser> pg = userMapper.selectPage(page, qw);

        // 关联角色
        Map<Long, SysRole> roleMap = new HashMap<>();
        if (pg.getRecords() != null && !pg.getRecords().isEmpty()) {
            List<Long> rids = pg.getRecords().stream()
                    .map(SysUser::getRoleId).distinct().collect(Collectors.toList());
            for (SysRole r : roleMapper.selectBatchIds(rids)) {
                roleMap.put(r.getId(), r);
            }
        }
        IPage<UserVO> voPage = pg.convert(u -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(u, vo, "password");
            SysRole r = u.getRoleId() == null ? null : roleMap.get(u.getRoleId());
            if (r != null) {
                vo.setRoleCode(r.getCode());
                vo.setRoleName(r.getName());
            }
            return vo;
        });
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(UserCreateRequest req, Long currentUserId) {
        // 3a 账号名重复
        if (userMapper.selectByUsername(req.getUsername()) != null) {
            throw new BizException(40021, "账号名已存在");
        }
        SysRole role = roleMapper.selectById(req.getRoleId());
        if (role == null) {
            throw new BizException(40022, "角色不存在");
        }
        SysUser u = new SysUser();
        u.setUsername(req.getUsername());
        u.setRealName(req.getRealName());
        u.setRoleId(req.getRoleId());
        u.setEmployeeId(req.getEmployeeId());
        u.setStatus(1);
        u.setFailCount(0);
        String raw = (req.getInitialPassword() == null || req.getInitialPassword().isBlank())
                ? initialPwd : req.getInitialPassword();
        u.setPassword(PasswordUtil.hash(raw));
        u.setCreateTime(LocalDateTime.now());
        u.setUpdateTime(LocalDateTime.now());
        u.setCreateBy(currentUserId);
        u.setUpdateBy(currentUserId);
        userMapper.insert(u);
        return u.getId();
    }

    @Override
    public void enable(Long id, Long currentUserId) {
        SysUser u = mustGet(id);
        u.setStatus(1);
        u.setFailCount(0);
        u.setLockedUntil(null);
        u.setUpdateTime(LocalDateTime.now());
        u.setUpdateBy(currentUserId);
        userMapper.updateById(u);
    }

    @Override
    public void disable(Long id, Long currentUserId) {
        // 4a 不能停用当前登录账号
        if (id.equals(currentUserId)) {
            throw new BizException(40023, "不能停用当前登录账号");
        }
        SysUser u = mustGet(id);
        // 5a 关联业务数据未处理（简化：employee_id 非空即认为有业务数据，禁止停用）
        if (u.getEmployeeId() != null) {
            throw new BizException(40024, "该账号存在业务数据，请先处理");
        }
        u.setStatus(0);
        u.setUpdateTime(LocalDateTime.now());
        u.setUpdateBy(currentUserId);
        userMapper.updateById(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, ResetPasswordRequest req, Long currentUserId) {
        SysUser u = mustGet(id);
        String raw = (req.getNewPassword() == null || req.getNewPassword().isBlank())
                ? initialPwd : req.getNewPassword();
        u.setPassword(PasswordUtil.hash(raw));
        u.setFailCount(0);
        u.setLockedUntil(null);
        u.setUpdateTime(LocalDateTime.now());
        u.setUpdateBy(currentUserId);
        userMapper.updateById(u);
    }

    private SysUser mustGet(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw new BizException(404, "账号不存在");
        }
        return u;
    }
}
