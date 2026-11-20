package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 劳动合同 新增 / 编辑 / 续签 请求体（UC-010 / UC-011）
 *   endDate >= startDate 用 @AssertTrue 校验（响应同组失败）
 *   合同编号唯一性由 Service 层校验
 */
@Data
public class ContractCreateRequest {

    /** 编辑/续签时必填；续签场景下表示被续签的原合同 id */
    private Long id;

    /** 续签专用：被续签的原合同 id（仅 UC-011 续签动作使用） */
    private Long renewFromId;

    @NotBlank(message = "请填写合同编号")
    private String contractNo;

    @NotNull(message = "请选择职工")
    private Long employeeId;

    @NotBlank(message = "请填写合同类型")
    private String contractType;

    @NotNull(message = "请选择签订日期")
    private LocalDate signDate;

    @NotNull(message = "请选择开始日期")
    private LocalDate startDate;

    @NotNull(message = "请选择结束日期")
    private LocalDate endDate;

    /** 结束日期不能早于开始日期 */
    @AssertTrue(message = "合同结束日期不能早于开始日期")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    private Integer probationMonths;
    private Integer termMonths;
    private String remark;
}