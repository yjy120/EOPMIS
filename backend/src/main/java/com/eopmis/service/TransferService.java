package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.TransferRequest;
import com.eopmis.dto.TransferVO;

/**
 * 人事调动服务接口（UC-013）
 */
public interface TransferService {

    /** 分页查询调动记录 */
    IPage<TransferVO> page(int pageNo, int pageSize,
                           Long employeeId, Integer status);

    /** 调动详情 */
    TransferVO detail(Long id);

    /** 发起调动：写 sys_transfer + 同步更新 sys_employee.dept_id/position_id（事务） */
    Long create(TransferRequest req, Long currentUserId);

    /** 编辑调动（仅基础字段，状态不可改） */
    void update(TransferRequest req, Long currentUserId);

    /** 删除调动 */
    void delete(Long id, Long currentUserId);

    /** 撤销调动：把 status=2，但不动 sys_employee（保留历史） */
    void revoke(Long id, Long currentUserId);
}