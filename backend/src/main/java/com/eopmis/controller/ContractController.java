package com.eopmis.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eopmis.common.Result;
import com.eopmis.dto.ContractCreateRequest;
import com.eopmis.dto.ContractTerminateRequest;
import com.eopmis.dto.ContractVO;
import com.eopmis.security.CurrentUser;
import com.eopmis.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * UC-010 登记劳动合同 / UC-011 续签与终止劳动合同（HR 端）
 *   仅人事专员可访问（类级 @PreAuthorize 拦截）
 */
@RestController
@RequestMapping("/hr/contract")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final CurrentUser currentUser;

    @GetMapping("/page")
    public Result<IPage<ContractVO>> page(@RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "20") int pageSize,
                                          @RequestParam(required = false) String contractNo,
                                          @RequestParam(required = false) Long employeeId,
                                          @RequestParam(required = false) Integer status) {
        return Result.ok(contractService.page(pageNo, pageSize, contractNo, employeeId, status));
    }

    @GetMapping("/{id}")
    public Result<ContractVO> detail(@PathVariable Long id) {
        return Result.ok(contractService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ContractCreateRequest req) {
        return Result.ok(contractService.create(req, currentUser.require().getId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ContractCreateRequest req) {
        contractService.update(req, currentUser.require().getId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.delete(id, currentUser.require().getId());
        return Result.ok();
    }

    /**
     * UC-011 续签：路径参数为被续签的原合同 id，请求体为新合同字段
     */
    @PostMapping("/{id}/renew")
    public Result<Long> renew(@PathVariable Long id,
                              @Valid @RequestBody ContractCreateRequest newContract) {
        return Result.ok(contractService.renew(id, newContract, currentUser.require().getId()));
    }

    /**
     * UC-011 终止：路径参数为被终止合同 id
     */
    @PostMapping("/{id}/terminate")
    public Result<Void> terminate(@PathVariable Long id,
                                  @Valid @RequestBody ContractTerminateRequest req) {
        contractService.terminate(id, req, currentUser.require().getId());
        return Result.ok();
    }
}