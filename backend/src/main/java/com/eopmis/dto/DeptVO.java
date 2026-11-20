package com.eopmis.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门 VO（列表 / 详情 / 树节点通用）
 */
@Data
public class DeptVO {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String leader;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
