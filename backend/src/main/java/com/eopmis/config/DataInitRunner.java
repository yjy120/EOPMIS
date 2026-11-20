package com.eopmis.config;

import com.eopmis.entity.SysRole;
import com.eopmis.entity.SysUser;
import com.eopmis.mapper.SysRoleMapper;
import com.eopmis.mapper.SysUserMapper;
import com.eopmis.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 启动时种子数据修正器
 *   - 角色：存在即跳过
 *   - 账号：存在则校验密码是否可被 "YOUR_ADMIN_PWD" 验证，不通过则重置为该值
 *           （方便演示/测试环境；生产应去掉）
 *   - 不会插入重复数据
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitRunner implements CommandLineRunner {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;

    /**
     * 启动时是否强制重置种子账号密码为默认值
     *   true：测试/演示友好；false：生产环境必设
     */
    @Value("${password.force-reset-on-startup:true}")
    private boolean forceResetOnStartup;

    private static final String DEFAULT_ADMIN_PWD = "YOUR_ADMIN_PWD";

    @Override
    public void run(String... args) {
        // 1) 角色：仅确保三个内置角色存在
        ensureRole("ADMIN",    "系统管理员", 1, "管账号、角色、字典、审计");
        ensureRole("HR",       "人事专员",   1, "人事业务数据全权限");
        ensureRole("EMPLOYEE", "普通职工",   1, "只读本人数据 + 改密");

        // 2) 账号：保证 admin/hr01/emp01 存在，且密码是已知明文
        SysRole adminRole = roleMapper.selectById(1L);
        SysRole hrRole    = roleMapper.selectById(2L);
        SysRole empRole   = roleMapper.selectById(3L);

        // employeeId 暂传 null：sys_employee 表尚未建立（UC-007 范围），
        // 待员工档案功能落地后由 SQL 或 UserAdminServiceImpl 显式绑定
        ensureUser("admin", "杨佳莹", adminRole.getId(), null,  DEFAULT_ADMIN_PWD);
        ensureUser("hr01",  "于悦",   hrRole.getId(),    null,  "Hr@YOUR_DB_PASSWORD");
        ensureUser("emp01", "王岩",   empRole.getId(),   null,  "Emp@YOUR_DB_PASSWORD");
        // 演示用普通员工账号（UC-007~017 联调数据），每人独立密码
        ensureUser("emp02", "李明华", empRole.getId(), null, "Emp02@YOUR_DB_PASSWORD");
        ensureUser("emp03", "陈思雨", empRole.getId(), null, "Emp03@YOUR_DB_PASSWORD");
        ensureUser("emp04", "王建国", empRole.getId(), null, "Emp04@YOUR_DB_PASSWORD");
        ensureUser("emp05", "刘小芳", empRole.getId(), null, "Emp05@YOUR_DB_PASSWORD");
        ensureUser("emp06", "赵敏",   empRole.getId(), null, "Emp06@YOUR_DB_PASSWORD");
        ensureUser("emp07", "孙丽",   empRole.getId(), null, "Emp07@YOUR_DB_PASSWORD");
        ensureUser("emp08", "周强",   empRole.getId(), null, "Emp08@YOUR_DB_PASSWORD");
        ensureUser("emp09", "吴磊",   empRole.getId(), null, "Emp09@YOUR_DB_PASSWORD");

        log.info("[DataInitRunner] 种子数据准备完毕。可用账号：admin/{} | hr01/Hr@YOUR_DB_PASSWORD | emp01~emp09（每人独立密码，格式 Emp0X@YOUR_DB_PASSWORD）（forceResetOnStartup={}）",
                DEFAULT_ADMIN_PWD, forceResetOnStartup);
    }

    private void ensureRole(String code, String name, int isSystem, String desc) {
        SysRole r = roleMapper.selectById(codeToId(code));
        if (r == null) {
            // 角色没有按 code 查的方法，简单按 id 1/2/3
            long id = codeToId(code);
            r = new SysRole();
            r.setId(id);
            r.setCode(code);
            r.setName(name);
            r.setDescription(desc);
            r.setIsSystem(isSystem);
            r.setCreateTime(LocalDateTime.now());
            r.setUpdateTime(LocalDateTime.now());
            roleMapper.insert(r);
            log.info("[DataInitRunner] 角色 {} 已创建", code);
        }
    }

    private long codeToId(String code) {
        switch (code) {
            case "ADMIN":    return 1L;
            case "HR":       return 2L;
            case "EMPLOYEE": return 3L;
            default: throw new IllegalArgumentException(code);
        }
    }

    private void ensureUser(String username, String realName, Long roleId, Long employeeId, String pwd) {
        SysUser u = userMapper.selectByUsername(username);
        String hashed = PasswordUtil.hash(pwd);
        if (u == null) {
            u = new SysUser();
            u.setUsername(username);
            u.setRealName(realName);
            u.setPassword(hashed);
            u.setStatus(1);
            u.setRoleId(roleId);
            u.setEmployeeId(employeeId);
            u.setFailCount(0);
            u.setCreateTime(LocalDateTime.now());
            u.setUpdateTime(LocalDateTime.now());
            userMapper.insert(u);
            log.info("[DataInitRunner] 账号 {} 已创建", username);
        } else if (forceResetOnStartup && !PasswordUtil.matches(pwd, u.getPassword())) {
            // 仅在 force-reset-on-startup=true 时重置密码（生产环境应置为 false）
            u.setPassword(hashed);
            u.setStatus(1);
            u.setFailCount(0);
            u.setLockedUntil(null);
            u.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(u);
            log.info("[DataInitRunner] 账号 {} 密码已重置（force-reset-on-startup=true）", username);
        } else if (!forceResetOnStartup) {
            log.debug("[DataInitRunner] 账号 {} 已存在，跳过密码重置（force-reset-on-startup=false）", username);
        }
    }
}
