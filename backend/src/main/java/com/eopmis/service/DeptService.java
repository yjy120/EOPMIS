package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.DeptCreateRequest;
import com.eopmis.dto.DeptTreeNode;
import com.eopmis.dto.DeptVO;

import java.util.List;

/**
 * UC-005 部门管理 Service
 */
public interface DeptService {

    /** 分页查询（name 模糊、parentId 精确） */
    IPage<DeptVO> page(int pageNo, int pageSize, String name, Long parentId);

    /** 全部部门（平铺，给前端构造树或下拉） */
    List<DeptVO> listAll();

    /** 部门树（保留以备后续使用） */
    List<DeptTreeNode> tree();

    DeptVO detail(Long id);

    Long create(DeptCreateRequest req, Long currentUserId);

    void update(DeptCreateRequest req, Long currentUserId);

    void delete(Long id, Long currentUserId);
}
