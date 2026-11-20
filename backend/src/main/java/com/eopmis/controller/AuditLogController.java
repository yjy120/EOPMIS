package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eopmis.common.Result;
import com.eopmis.entity.SysAuditLog;
import com.eopmis.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志查询接口（只读）
 *   对应菜单 sys:audit:view，仅系统管理员可访问
 *   仅提供分页查询，不提供增删改（审计日志不可篡改）
 */
@RestController
@RequestMapping("/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final SysAuditLogMapper auditLogMapper;

    @GetMapping("/page")
    public Result<IPage<SysAuditLog>> page(@RequestParam(defaultValue = "1") int pageNo,
                                           @RequestParam(defaultValue = "20") int pageSize) {
        Page<SysAuditLog> page = new Page<>(pageNo, pageSize);
        IPage<SysAuditLog> pg = auditLogMapper.selectPage(page, null);
        // 按创建时间倒序
        pg.getRecords().sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        return Result.ok(pg);
    }

    @GetMapping("/count")
    public Result<Long> count() {
        return Result.ok(auditLogMapper.selectCount(null));
    }
}
