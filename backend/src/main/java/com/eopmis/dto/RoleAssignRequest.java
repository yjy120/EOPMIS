package com.eopmis.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求
 */
@Data
public class RoleAssignRequest {

    private Long roleId;
    private List<Long> menuIds;
}
