package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.ContractCreateRequest;
import com.eopmis.dto.ContractTerminateRequest;
import com.eopmis.dto.ContractVO;

import java.util.List;

/**
 * 劳动合同服务接口（UC-010 / UC-011 / UC-012）
 */
public interface ContractService {

    /** 分页查询合同（UC-010 HR 端） */
    IPage<ContractVO> page(int pageNo, int pageSize,
                           String contractNo, Long employeeId, Integer status);

    /** 合同详情 */
    ContractVO detail(Long id);

    /** 新增合同（UC-010 主成功场景） */
    Long create(ContractCreateRequest req, Long currentUserId);

    /** 编辑合同（UC-010 PRD FR-011 含修改） */
    void update(ContractCreateRequest req, Long currentUserId);

    /** 删除合同（软删除） */
    void delete(Long id, Long currentUserId);

    /** 续签合同（UC-011 主成功场景 2/3）：原合同→已到期 + 新合同=有效，事务 */
    Long renew(Long renewFromId, ContractCreateRequest newContractReq, Long currentUserId);

    /** 终止合同（UC-011 主成功场景 4） */
    void terminate(Long id, ContractTerminateRequest req, Long currentUserId);

    /** UC-012 当前登录职工的合同列表（不可越权） */
    List<ContractVO> myContracts(Long currentUserId);
}