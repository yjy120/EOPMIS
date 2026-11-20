package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单/权限点
 *   type: 1=目录 2=菜单 3=按钮/接口
 *   perm: 权限标识（@PreAuthorize 使用）
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单 ID，0=根 */
    private Long parentId;

    private String name;

    /** 权限标识，如 sys:user:add */
    private String perm;

    /** 类型：1=目录 2=菜单 3=按钮 */
    private Integer type;

    /** 路由/前端路径 */
    private String path;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
