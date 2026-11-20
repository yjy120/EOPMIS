package com.eopmis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eopmis.entity.SysEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 职工档案 Mapper（UC-007/008/009）
 *   自定义方法全部使用 @Select 内联 SQL
 */
@Mapper
public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {

    /**
     * 校验职工编号唯一（排除自身）
     */
    @Select("SELECT COUNT(*) FROM sys_employee WHERE emp_no = #{empNo} AND deleted = 0 AND id <> #{excludeId}")
    int countByEmpNo(@Param("empNo") String empNo, @Param("excludeId") Long excludeId);

    /**
     * 校验身份证号唯一（排除自身）
     */
    @Select("SELECT COUNT(*) FROM sys_employee WHERE id_card = #{idCard} AND deleted = 0 AND id <> #{excludeId}")
    int countByIdCard(@Param("idCard") String idCard, @Param("excludeId") Long excludeId);

    /**
     * 根据 sys_user.id 定位绑定的 employee（UC-009 本人档案）
     */
    @Select("SELECT * FROM sys_employee WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    SysEmployee selectByUserId(@Param("userId") Long userId);

    /**
     * 校验登录账号是否已被其他档案绑定（excludeId 排除自身）
     */
    @Select("SELECT COUNT(*) FROM sys_employee WHERE user_id = #{userId} AND deleted = 0 AND id <> #{excludeId}")
    int countByUserId(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
}