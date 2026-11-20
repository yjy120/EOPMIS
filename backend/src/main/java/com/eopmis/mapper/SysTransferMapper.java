package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysTransfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人事调动 Mapper（UC-013）
 *   仅基础 CRUD，校验放 Service 层
 */
@Mapper
public interface SysTransferMapper extends BaseMapper<SysTransfer> {
}