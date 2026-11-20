package com.eopmis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eopmis.common.BizException;
import com.eopmis.dto.ChangePasswordRequest;
import com.eopmis.dto.LoginRequest;
import com.eopmis.dto.LoginResponse;
import com.eopmis.entity.*;
import com.eopmis.mapper.*;
import com.eopmis.service.AuthService;
import com.eopmis.util.JwtUtil;
import com.eopmis.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 登录 / 改密 / 登出
 *   实现 UC-001 与 UC-002
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysPasswordHistoryMapper historyMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final JwtUtil jwtUtil;

    @Value("${jwt.lock-minutes}")
    private int lockMinutes;

    @Value("${jwt.max-fail-count}")
    private int maxFailCount;

    @Value("${password.history-keep}")
    private int historyKeep;

    @Value("${password.min-length}")
    private int pwdMinLen;

    @Value("${password.require-letter}")
    private boolean requireLetter;

    @Value("${password.require-digit}")
    private boolean requireDigit;

    private static final Pattern P_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern P_DIGIT  = Pattern.compile(".*[0-9].*");

    // ---------------- UC-001 登录 ----------------
    @Override
    public LoginResponse login(LoginRequest req, String clientIp) {
        SysUser user = userMapper.selectByUsername(req.getUsername());

        // 4a 账号不存在（与密码错误同提示，防账号枚举）
        boolean notFound = (user == null);
        if (notFound) {
            writeAudit(null, req.getUsername(), "AUTH", "LOGIN_FAIL", "账号不存在", clientIp, 0);
            throw new BizException(40001, "账号或密码错误");
        }

        // 锁定校验
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            writeAudit(user.getId(), user.getUsername(), "AUTH", "LOGIN_FAIL", "账号被锁定", clientIp, 0);
            throw new BizException(40002,
                    "账号已锁定，请于 " + user.getLockedUntil() + " 后重试");
        }

        // 4b 密码错误
        if (!PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            int fails = (user.getFailCount() == null ? 0 : user.getFailCount()) + 1;
            user.setFailCount(fails);
            if (fails >= maxFailCount) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
                user.setFailCount(0); // 重置计数，下次再错重新累计
            }
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            String msg = fails >= maxFailCount
                    ? "连续错误" + maxFailCount + "次，账号已锁定" + lockMinutes + "分钟"
                    : "账号或密码错误（已错误" + fails + "次，超过" + maxFailCount + "次将锁定）";
            writeAudit(user.getId(), user.getUsername(), "AUTH", "LOGIN_FAIL", msg, clientIp, 0);
            throw new BizException(40001, msg);
        }

        // 5a 账号停用
        if (user.getStatus() == null || user.getStatus() != 1) {
            writeAudit(user.getId(), user.getUsername(), "AUTH", "LOGIN_FAIL", "账号已停用", clientIp, 0);
            throw new BizException(40003, "账号已停用，请联系系统管理员");
        }

        // 主成功场景
        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new BizException(500, "账号未分配角色");
        }
        List<SysMenu> menus = menuMapper.selectByRoleId(role.getId());
        List<String> perms = menus.stream()
                .map(SysMenu::getPerm)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 构造菜单树
        List<LoginResponse.MenuNode> tree = buildMenuTree(menus, 0L);

        // 重置失败计数 + 写最后登录
        user.setFailCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 签发 token
        String token = jwtUtil.generate(user.getId(), user.getUsername(), role.getCode());
        writeAudit(user.getId(), user.getUsername(), "AUTH", "LOGIN", "登录成功", clientIp, 1);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setExpiresIn(jwtUtil.getExpiration() / 1000);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setRoleCode(role.getCode());
        resp.setRoleName(role.getName());
        resp.setPermissions(perms);
        resp.setMenus(tree);
        return resp;
    }

    // ---------------- UC-002 修改密码 ----------------
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPwd, String newPwd, String confirmPwd) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "账号不存在");
        }

        // 4a 旧密码错误
        if (!PasswordUtil.matches(oldPwd, user.getPassword())) {
            throw new BizException(40011, "旧密码错误");
        }

        // 7a 两次输入不一致
        if (!Objects.equals(newPwd, confirmPwd)) {
            throw new BizException(40012, "两次输入的新密码不一致");
        }

        // 5a 密码策略
        validatePasswordPolicy(newPwd);

        // 6a 历史重复
        List<SysPasswordHistory> history = historyMapper.selectRecentByUserId(userId, historyKeep);
        for (SysPasswordHistory h : history) {
            if (PasswordUtil.matches(newPwd, h.getPassword())) {
                throw new BizException(40013, "新密码不能与最近 " + historyKeep + " 次使用过的密码相同");
            }
        }

        // 7 写入
        String hashed = PasswordUtil.hash(newPwd);
        user.setPassword(hashed);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        SysPasswordHistory h = new SysPasswordHistory();
        h.setUserId(userId);
        h.setPassword(hashed);
        h.setCreateTime(LocalDateTime.now());
        historyMapper.insert(h);

        writeAudit(userId, user.getUsername(), "AUTH", "UPDATE_PASSWORD", "改密成功", null, 1);
    }

    // ---------------- 登出 ----------------
    @Override
    public void logout(String username, String clientIp) {
        if (username == null) return;
        SysUser user = userMapper.selectByUsername(username);
        writeAudit(user == null ? null : user.getId(), username, "AUTH", "LOGOUT", "登出", clientIp, 1);
    }

    // ---------------- 私有方法 ----------------
    private void validatePasswordPolicy(String pwd) {
        if (pwd == null || pwd.length() < pwdMinLen) {
            throw new BizException(40014, "新密码长度不能少于 " + pwdMinLen + " 位");
        }
        if (requireLetter && !P_LETTER.matcher(pwd).matches()) {
            throw new BizException(40014, "新密码必须包含字母");
        }
        if (requireDigit && !P_DIGIT.matcher(pwd).matches()) {
            throw new BizException(40014, "新密码必须包含数字");
        }
    }

    private void writeAudit(Long userId, String username, String module, String action,
                            String detail, String ip, int result) {
        try {
            SysAuditLog log = new SysAuditLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setModule(module);
            log.setAction(action);
            log.setDetail(detail);
            log.setIp(ip);
            log.setResult(result);
            log.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception ignore) {
            // 审计写库失败不影响业务
        }
    }

    private List<LoginResponse.MenuNode> buildMenuTree(List<SysMenu> all, Long parentId) {
        List<LoginResponse.MenuNode> roots = new ArrayList<>();
        if (CollUtil.isEmpty(all)) return roots;
        Map<Long, List<SysMenu>> byParent = all.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        for (SysMenu m : all) {
            if (Objects.equals(m.getParentId(), parentId)) {
                LoginResponse.MenuNode n = new LoginResponse.MenuNode();
                BeanUtil.copyProperties(m, n);
                n.setChildren(buildMenuTree(byParent.getOrDefault(m.getId(), Collections.emptyList()), m.getId()));
                roots.add(n);
            }
        }
        return roots;
    }
}
