package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.DeptCreateRequest;
import com.eopmis.dto.DeptTreeNode;
import com.eopmis.dto.DeptVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * UC-005 部门管理接口
 *   仅人事专员 HR 可访问（类级 @PreAuthorize 拦截）
 */
@RestController
@RequestMapping("/hr/dept")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<DeptVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                      @RequestParam(defaultValue = "20") int pageSize,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) Long parentId) {
        return Result.ok(deptService.page(pageNo, pageSize, name, parentId));
    }

    @GetMapping("/all")
    public Result<List<DeptVO>> all() {
        return Result.ok(deptService.listAll());
    }

    @GetMapping("/tree")
    public Result<List<DeptTreeNode>> tree() {
        return Result.ok(deptService.tree());
    }

    @GetMapping("/{id}")
    public Result<DeptVO> detail(@PathVariable Long id) {
        return Result.ok(deptService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody DeptCreateRequest req) {
        return Result.ok(deptService.create(req, currentUser.require().getId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody DeptCreateRequest req) {
        deptService.update(req, currentUser.require().getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id, currentUser.require().getId());
        return Result.ok();
    }
}
