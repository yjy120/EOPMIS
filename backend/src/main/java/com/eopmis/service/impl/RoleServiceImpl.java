package com.eopmis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eopmis.common.BizException;
import com.eopmis.dto.RoleAssignRequest;
import com.eopmis.dto.RoleVO;
import com.eopmis.entity.SysMenu;
import com.eopmis.entity.SysRole;
import com.eopmis.entity.SysRoleMenu;
import com.eopmis.entity.SysUser;
import com.eopmis.mapper.SysMenuMapper;
import com.eopmis.mapper.SysRoleMapper;
import com.eopmis.mapper.SysUserMapper;
import com.eopmis.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UC-004 角色与权限分配
 *   列出全部角色、查看某角色权限点、为角色分配权限
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserMapper userMapper;

    @Override
    public List<RoleVO> listAll() {
        List<SysRole> roles = roleMapper.selectList(new QueryWrapper<SysRole>().orderByAsc("id"));
        return roles.stream().map(r -> {
            RoleVO vo = new RoleVO();
            vo.setId(r.getId());
            vo.setCode(r.getCode());
            vo.setName(r.getName());
            vo.setDescription(r.getDescription());
            vo.setIsSystem(r.getIsSystem());
            List<SysMenu> menus = menuMapper.selectByRoleId(r.getId());
            vo.setMenuIds(menus.stream().map(SysMenu::getId).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public RoleVO detail(Long id) {
        SysRole r = roleMapper.selectById(id);
        if (r == null) throw new BizException(404, "角色不存在");
        RoleVO vo = new RoleVO();
        vo.setId(r.getId());
        vo.setCode(r.getCode());
        vo.setName(r.getName());
        vo.setDescription(r.getDescription());
        vo.setIsSystem(r.getIsSystem());
        List<SysMenu> menus = menuMapper.selectByRoleId(r.getId());
        vo.setMenuIds(menus.stream().map(SysMenu::getId).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(RoleAssignRequest req) {
        saveMenus(req.getRoleId(), req.getMenuIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) throw new BizException(400, "roleId 不能为空");
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) throw new BizException(404, "角色不存在");

        // 2a 至少一个菜单
        if (menuIds == null || menuIds.isEmpty()) {
            throw new BizException(40031, "角色至少需要一个可用菜单权限");
        }

        // 3a 角色被引用：若有用户绑定该角色，禁止调整权限（按需求约定）
        Long refCount = userMapper.selectCount(new QueryWrapper<SysUser>()
                .eq("role_id", roleId).eq("status", 1));
        if (refCount != null && refCount > 0) {
            // 注：UC-004 需求是"提示被引用，请先调整用户角色"——简单实现为禁止调整已启用账号引用的角色
            // 如业务允许"先记录日志再调整"，可放宽；本期按严格实现
            throw new BizException(40032, "该角色仍被用户使用，请先调整用户角色");
        }

        // 删除旧关联
        menuMapper.deleteByRoleId(roleId);
        // 批量插入
        List<SysRoleMenu> list = new ArrayList<>();
        for (Long mid : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(mid);
            list.add(rm);
        }
        menuMapper.insertBatch(list);
    }
}
