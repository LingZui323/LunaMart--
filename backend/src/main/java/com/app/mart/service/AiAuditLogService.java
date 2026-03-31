package com.app.mart.service;

import com.app.mart.entity.AiAuditLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * AI审核日志业务接口
 * @author LunaMart
 */
public interface AiAuditLogService extends IService<AiAuditLog> {

    /**
     * 插入日志
     */
    void insertLog(AiAuditLog log);

    /**
     * 分页查询日志（管理员）
     */
    Page<AiAuditLog> getLogPage(Integer page, Integer size);
}