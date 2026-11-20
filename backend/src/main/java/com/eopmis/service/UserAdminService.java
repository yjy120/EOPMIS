package com.eopmis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.dto.ResetPasswordRequest;
import com.eopmis.dto.UserCreateRequest;
import com.eopmis.dto.UserVO;

public interface UserAdminService {

    IPage<UserVO> page(int pageNo, int pageSize, String keyword, Integer status, Long roleId);

    Long create(UserCreateRequest req, Long currentUserId);

    void enable(Long id, Long currentUserId);

    void disable(Long id, Long currentUserId);

    void resetPassword(Long id, ResetPasswordRequest req, Long currentUserId);
}
