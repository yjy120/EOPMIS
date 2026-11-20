package com.eopmis.service;

import com.eopmis.dto.LoginRequest;
import com.eopmis.dto.LoginResponse;

public interface AuthService {

    /**
     * 登录
     * @throws com.eopmis.common.BizException 4a 账号不存在/密码错误；4b 锁定；5a 停用
     */
    LoginResponse login(LoginRequest req, String clientIp);

    /**
     * 修改本人密码
     */
    void changePassword(Long userId, String oldPwd, String newPwd, String confirmPwd);

    /**
     * 退出登录（仅审计）
     */
    void logout(String username, String clientIp);
}
