import request from '@/utils/request'

// 审计日志查询（仅系统管理员）
export const pageAuditLogs = (params) => request.get('/admin/audit/page', { params })
export const countAuditLogs = () => request.get('/admin/audit/count')
