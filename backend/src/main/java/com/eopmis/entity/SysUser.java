package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户账号
 *   status: 0=停用 1=正常
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号，唯一 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** BCrypt 密码 */
    private String password;

    /** 状态：0=停用 1=正常 */
    private Integer status;

    /** 角色 ID（单角色） */
    private Long roleId;

    /** 绑定的员工 ID（普通职工用） */
    private Long employeeId;

    /** 连续登录失败次数 */
    private Integer failCount;

    /** 锁定截止时间 */
    private LocalDateTime lockedUntil;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;

    /** 最后登录 IP */
    private String lastLoginIp;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}
