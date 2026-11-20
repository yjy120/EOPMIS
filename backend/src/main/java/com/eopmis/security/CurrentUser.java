package com.eopmis.security;

import com.eopmis.common.BizException;
import com.eopmis.entity.SysUser;
import com.eopmis.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户工具
 *   业务层需要操作"当前用户"时通过该 Bean 拿 sys_user
 */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final SysUserMapper userMapper;

    public SysUser require() {
        String username = currentUsername();
        if (username == null) {
            throw new BizException(401, "未登录");
        }
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(401, "账号不存在");
        }
        return user;
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
