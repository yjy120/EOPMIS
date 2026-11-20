package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 职工档案（UC-007 / UC-008 / UC-009）
 *   status: 0=离职 1=在职
 *   user_id: 绑定的登录账号 ID（普通职工用；HR 账号一般不绑定）
 *   软删除：deleted=0
 */
@Data
@TableName("sys_employee")
public class SysEmployee {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 职工编号，全局唯一 */
    private String empNo;

    private String name;

    /** 1=男 2=女 */
    private Integer gender;

    private LocalDate birthDate;

    /** 身份证号，全局唯一 */
    private String idCard;

    private String phone;

    private Long deptId;
    private Long positionId;

    /** 职级 P1/P2/... */
    private String level;

    private LocalDate entryDate;

    /** 0=离职 1=在职 */
    private Integer status;

    /** 绑定的登录账号 ID */
    private Long userId;

    private String remark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}