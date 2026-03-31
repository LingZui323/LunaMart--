package com.app.mart.service.impl;

import com.app.mart.entity.AiAuditLog;
import com.app.mart.mapper.AiAuditLogMapper;
import com.app.mart.service.AiAuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * AI 内容审核日志服务实现类
 * 负责记录商品图片、内容的 AI 审核记录，支持日志插入与分页查询
 *
 * @author LunaMart
 */
@Service
public class AiAuditLogServiceImpl extends ServiceImpl<AiAuditLogMapper, AiAuditLog> implements AiAuditLogService {

    /**
     * 插入 AI 审核日志
     * @param log 日志实体
     */
    @Override
    public void insertLog(AiAuditLog log) {
        save(log);
    }

    /**
     * 分页查询 AI 审核日志（仅商品类型，按创建时间倒序）
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    @Override
    public Page<AiAuditLog> getLogPage(Integer page, Integer size) {
        LambdaQueryWrapper<AiAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAuditLog::getTargetType, "GOODS");
        wrapper.orderByDesc(AiAuditLog::getCreateTime);

        return page(new Page<>(page, size), wrapper);
    }
}