package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色
 *   code: ADMIN / HR / EMPLOYEE
 *   isSystem: 1=系统内置（不可删除）
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private String description;

    /** 1=系统内置 */
    private Integer isSystem;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
