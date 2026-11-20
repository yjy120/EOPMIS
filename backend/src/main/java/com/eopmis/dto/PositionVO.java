package com.eopmis.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位 VO（列表 / 详情）
 *   deptName 由 service 层冗余填充，便于前端展示
 */
@Data
public class PositionVO {
    private Long id;
    private Long deptId;
    private String deptName;
    private String name;
    private String code;
    private String description;
    private String level;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
