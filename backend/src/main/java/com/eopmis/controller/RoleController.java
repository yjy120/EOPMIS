package com.eopmis.controller;

import com.eopmis.common.Result;
import com.eopmis.dto.PermissionVO;
import com.eopmis.dto.RoleAssignRequest;
import com.eopmis.dto.RoleVO;
import com.eopmis.mapper.SysMenuMapper;
import com.eopmis.service.RoleService;
import com.eopmis.entity.SysMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UC-004 角色与权限管理
 *   仅系统管理员
 */
@RestController
@RequestMapping("/admin/role")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final SysMenuMapper menuMapper;

    @GetMapping
    public Result<List<RoleVO>> list() {
        return Result.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<RoleVO> detail(@PathVariable Long id) {
        return Result.ok(roleService.detail(id));
    }

    @PostMapping("/assign")
    public Result<Void> assign(@RequestBody RoleAssignRequest req) {
        roleService.assignMenus(req);
        return Result.ok();
    }

    @GetMapping("/menu/all")
    public Result<List<PermissionVO>> allMenus() {
        List<SysMenu> all = menuMapper.selectList(null);
        return Result.ok(all.stream().map(m -> {
            PermissionVO v = new PermissionVO();
            v.setId(m.getId());
            v.setParentId(m.getParentId());
            v.setName(m.getName());
            v.setCode(m.getPerm());
            v.setType(m.getType());
            v.setPath(m.getPath());
            v.setIcon(m.getIcon());
            v.setSort(m.getSort());
            return v;
        }).collect(Collectors.toList()));
    }
}
