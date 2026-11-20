package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysPasswordHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPasswordHistoryMapper extends BaseMapper<SysPasswordHistory> {

    /**
     * 取用户最近 N 条密码历史
     */
    List<SysPasswordHistory> selectRecentByUserId(@Param("userId") Long userId,
                                                  @Param("limit") int limit);
}
