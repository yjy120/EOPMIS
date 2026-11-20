package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 终止合同请求体（UC-011 终止动作）
 *   终止日期≥合同开始日期 由 Service 层校验（需查原合同）
 */
@Data
public class ContractTerminateRequest {

    @NotNull(message = "请选择终止日期")
    private LocalDate terminateDate;

    /** 终止原因，可空 */
    private String terminateReason;
}