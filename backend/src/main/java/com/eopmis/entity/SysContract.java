package com.eopmis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 劳动合同（UC-010 / UC-011）
 *   status: 1=有效 2=即将到期 3=已到期 4=已终止
 *   软删除：deleted=0
 */
@Data
@TableName("sys_contract")
public class SysContract {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 合同编号（全局唯一） */
    private String contractNo;

    /** 职工 id */
    private Long employeeId;

    /** 合同类型：固定期限/无固定期限/实习 */
    private String contractType;

    /** 签订日期 */
    private LocalDate signDate;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 试用期（月） */
    private Integer probationMonths;

    /** 合同期限（月） */
    private Integer termMonths;

    /** 1=有效 2=即将到期 3=已到期 4=已终止 */
    private Integer status;

    /** 实际终止日期（终止时填） */
    private LocalDate terminateDate;

    /** 终止原因 */
    private String terminateReason;

    private String remark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}