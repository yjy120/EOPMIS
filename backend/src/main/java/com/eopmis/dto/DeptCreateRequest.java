package com.eopmis.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 新增 / 编辑部门（UC-005）
 *   顶级部门 parentId=0
 *   编辑时 id 必填
 */
@Data
public class DeptCreateRequest {

    /** 编辑时必填 */
    private Long id;

    /** 上级部门，0=顶级 */
    private Long parentId = 0L;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称不能超过 64 字")
    private String name;

    /** 部门编码（可空） */
    private String code;

    /** 负责人 */
    private String leader;

    /** 排序 */
    private Integer sort = 0;

    /** 状态：0=停用 1=正常 */
    private Integer status = 1;
}
