package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 部门 Mapper（UC-005）
 *   继承 BaseMapper 后自动拥有 selectPage / selectList / selectById / insert / updateById / deleteById
 *   自定义方法全部使用 @Select 内联 SQL（与 SysUserMapper 一致）
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 统计指定部门的下级部门数量（用于删除校验）
     */
    @Select("SELECT COUNT(*) FROM sys_dept WHERE parent_id = #{deptId} AND deleted = 0")
    int countChildren(@Param("deptId") Long deptId);

    /**
     * 校验同级同名：同一 parentId 下 name 重复的记录数
     *   excludeId 用于编辑时排除自身
     */
    @Select("SELECT COUNT(*) FROM sys_dept WHERE parent_id = #{parentId} AND name = #{name} AND deleted = 0 AND id <> #{excludeId}")
    int countSiblingSameName(@Param("parentId") Long parentId,
                             @Param("name") String name,
                             @Param("excludeId") Long excludeId);

    /**
     * 查询某部门的 parent_id（用于"上级不能是自身或下级"的上溯校验）
     *   返回 null 表示无上级
     */
    @Select("SELECT parent_id FROM sys_dept WHERE id = #{deptId} AND deleted = 0")
    Long selectParentId(@Param("deptId") Long deptId);

    /**
     * 统计该部门下当前在职职工数量（UC-007 落地后实现）
     *   用于删除校验：存在在职职工时禁止删除部门
     */
    @Select("SELECT COUNT(*) FROM sys_employee WHERE dept_id = #{deptId} AND status = 1 AND deleted = 0")
    int countActiveEmployees(@Param("deptId") Long deptId);
}
