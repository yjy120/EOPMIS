package com.eopmis.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * 密码工具：BCrypt 不可逆存储
 * 校验时使用 BCrypt.checkpw(明文, 密文)
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    public static String hash(String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return BCrypt.hashpw(raw, BCrypt.gensalt(10));
    }

    public static boolean matches(String raw, String hashed) {
        if (raw == null || hashed == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(raw, hashed);
        } catch (Exception ignore) {
            return false;
        }
    }
}
