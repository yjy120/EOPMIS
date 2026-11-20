package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 人事调动 请求体（UC-013）
 *   调动日期≥入职日期、调入部门/岗位存在 由 Service 层校验
 */
@Data
public class TransferRequest {

    /** 编辑时必填 */
    private Long id;

    @NotNull(message = "请选择职工")
    private Long employeeId;

    @NotNull(message = "请选择调出部门")
    private Long fromDeptId;

    @NotNull(message = "请选择原岗位")
    private Long fromPositionId;

    @NotNull(message = "请选择调入部门")
    private Long toDeptId;

    @NotNull(message = "请选择新岗位")
    private Long toPositionId;

    @NotNull(message = "请选择调动日期")
    private LocalDate transferDate;

    private String reason;
}