package com.eopmis.service;

import com.eopmis.dto.RoleAssignRequest;
import com.eopmis.dto.RoleVO;

import java.util.List;

public interface RoleService {
    List<RoleVO> listAll();
    RoleVO detail(Long id);
    void assignMenus(RoleAssignRequest req);
    void saveMenus(Long roleId, List<Long> menuIds);
}
