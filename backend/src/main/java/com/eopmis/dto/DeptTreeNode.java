package com.eopmis.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门树形节点（前端 el-tree-select 使用）
 */
@Data
public class DeptTreeNode {
    private Long id;
    private Long parentId;
    private String name;
    private List<DeptTreeNode> children = new ArrayList<>();
}
