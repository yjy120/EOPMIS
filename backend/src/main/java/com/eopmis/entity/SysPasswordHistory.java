package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密码历史
 *   仅用于"不能与最近 3 次相同"校验
 *   保存的是 BCrypt 密文
 */
@Data
@TableName("sys_password_history")
public class SysPasswordHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String password;
    private LocalDateTime createTime;
}
