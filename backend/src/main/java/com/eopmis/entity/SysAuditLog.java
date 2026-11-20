package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志
 *   action: LOGIN/LOGOUT/LOGIN_FAIL/UPDATE_PASSWORD/...
 *   result: 1=成功 0=失败
 *   业务用户只读，不可删
 */
@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String module;
    private String action;
    private String detail;
    private String ip;
    private Integer result;
    private LocalDateTime createTime;
}
