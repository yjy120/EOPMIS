package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人事调动（UC-013）
 *   status: 1=已生效 2=已撤销
 *   软删除：deleted=0
 */
@Data
@TableName("sys_transfer")
public class SysTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 职工 id */
    private Long employeeId;

    /** 调出部门 */
    private Long fromDeptId;

    /** 原岗位 */
    private Long fromPositionId;

    /** 调入部门 */
    private Long toDeptId;

    /** 新岗位 */
    private Long toPositionId;

    /** 调动日期 */
    private LocalDate transferDate;

    /** 调动原因 */
    private String reason;

    /** 1=已生效 2=已撤销 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}