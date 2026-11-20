package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 岗位 Mapper（UC-006）
 */
@Mapper
public interface SysPositionMapper extends BaseMapper<SysPosition> {

    /**
     * 校验同部门下岗位名称是否重复
     *   excludeId 用于编辑时排除自身
     */
    @Select("SELECT COUNT(*) FROM sys_position WHERE dept_id = #{deptId} AND name = #{name} AND deleted = 0 AND id <> #{excludeId}")
    int countSameNameInDept(@Param("deptId") Long deptId,
                            @Param("name") String name,
                            @Param("excludeId") Long excludeId);

    /**
     * 统计该岗位下当前在职职工数量（UC-007 落地后实现）
     *   用于删除校验：存在在职职工时禁止删除岗位
     */
    @Select("SELECT COUNT(*) FROM sys_employee WHERE position_id = #{positionId} AND status = 1 AND deleted = 0")
    int countActiveEmployees(@Param("positionId") Long positionId);
}
