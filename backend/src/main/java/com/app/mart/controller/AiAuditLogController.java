package com.app.mart.controller;

import com.app.mart.common.result.R;
import com.app.mart.entity.AiAuditLog;
import com.app.mart.service.AiAuditLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

/**
 * AI 审核日志 Controller
 * 功能：商品内容 AI 安全审核记录查询
 * 权限：仅管理员 / 超级管理员可查看
 *
 * @author LunaMart
 */
@RestController
@RequestMapping("/ai/audit/log")
@Api(tags = "AI审核日志模块")
public class AiAuditLogController {

    @Resource
    private AiAuditLogService aiAuditLogService;

    /**
     * 分页查询 AI 审核日志
     * 用于管理员查看商品违规审核记录
     *
     * @param page  当前页码
     * @param size  每页条数
     * @param role  当前登录用户角色
     * @return      分页日志列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询AI审核日志")
    public R<Page<AiAuditLog>> getLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @ApiIgnore @RequestAttribute String role
    ) {
        // 权限校验：仅管理员可查看日志
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            return R.fail("无权限，仅管理员可查看");
        }

        Page<AiAuditLog> logPage = aiAuditLogService.getLogPage(page, size);
        return R.ok(logPage);
    }
}