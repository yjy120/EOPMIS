package com.eopmis.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色 VO（带权限点 ID 列表）
 */
@Data
public class RoleVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer isSystem;
    private List<Long> menuIds;
}
