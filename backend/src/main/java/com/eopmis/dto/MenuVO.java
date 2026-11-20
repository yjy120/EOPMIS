package com.eopmis.dto;

import lombok.Data;

/**
 * 菜单/权限点 VO
 */
@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String name;
    private String perm;
    private Integer type;
    private String path;
    private String icon;
    private Integer sort;
}
