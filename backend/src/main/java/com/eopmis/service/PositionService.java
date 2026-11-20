package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.PositionCreateRequest;
import com.eopmis.dto.PositionVO;

import java.util.List;

/**
 * UC-006 岗位管理 Service
 */
public interface PositionService {

    IPage<PositionVO> page(int pageNo, int pageSize, Long deptId, String name);

    /** 给前端"按部门"下拉使用 */
    List<PositionVO> listByDept(Long deptId);

    PositionVO detail(Long id);

    Long create(PositionCreateRequest req, Long currentUserId);

    void update(PositionCreateRequest req, Long currentUserId);

    void delete(Long id, Long currentUserId);
}
