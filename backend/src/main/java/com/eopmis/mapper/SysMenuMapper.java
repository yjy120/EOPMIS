package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询角色拥有的菜单（按角色 ID）
     */
    List<SysMenu> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有菜单
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入
     */
    int insertBatch(@Param("list") List<com.eopmis.entity.SysRoleMenu> list);
}
