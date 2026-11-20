package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.PositionCreateRequest;
import com.eopmis.dto.PositionVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * UC-006 岗位管理接口
 *   仅人事专员 HR 可访问（类级 @PreAuthorize 拦截）
 */
@RestController
@RequestMapping("/hr/position")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<PositionVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "20") int pageSize,
                                          @RequestParam(required = false) Long deptId,
                                          @RequestParam(required = false) String name) {
        return Result.ok(positionService.page(pageNo, pageSize, deptId, name));
    }

    @GetMapping("/byDept/{deptId}")
    public Result<List<PositionVO>> listByDept(@PathVariable Long deptId) {
        return Result.ok(positionService.listByDept(deptId));
    }

    @GetMapping("/{id}")
    public Result<PositionVO> detail(@PathVariable Long id) {
        return Result.ok(positionService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody PositionCreateRequest req) {
        return Result.ok(positionService.create(req, currentUser.require().getId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody PositionCreateRequest req) {
        positionService.update(req, currentUser.require().getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        positionService.delete(id, currentUser.require().getId());
        return Result.ok();
    }
}
