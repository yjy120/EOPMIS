package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-菜单关联
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    private Long roleId;
    private Long menuId;
    private LocalDateTime createTime;
}
