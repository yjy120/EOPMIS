package com.eopmis.dto;

import lombok.Data;

/**
 * 菜单/权限点 VO
 */
@Data
public class PermissionVO {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private Integer type;
    private String path;
    private String icon;
    private Integer sort;
    private Integer status;
}
