package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位（UC-006）
 *   隶属于某个部门（dept_id）
 *   status: 0=停用 1=正常
 */
@Data
@TableName("sys_position")
public class SysPosition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deptId;
    private String name;
    private String code;
    private String description;
    private String level;
    private Integer sort;
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}
