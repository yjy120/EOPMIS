package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 新增 / 编辑岗位（UC-006）
 */
@Data
public class PositionCreateRequest {

    /** 编辑时必填 */
    private Long id;

    @NotNull(message = "所属部门不能为空")
    private Long deptId;

    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 64, message = "岗位名称不能超过 64 字")
    private String name;

    /** 岗位编码 */
    private String code;

    /** 岗位职责 */
    private String description;

    /** 职级 P1/P2/... */
    private String level;

    private Integer sort = 0;

    /** 状态：0=停用 1=正常 */
    private Integer status = 1;
}
