import request from '@/utils/request'

// UC-013 人事调动 HR 端 API
export const pageTransfers = (params) => request.get('/hr/transfer/page', { params })
export const getTransfer = (id) => request.get(`/hr/transfer/${id}`)
export const createTransfer = (data) => request.post('/hr/transfer', data)
export const updateTransfer = (data) => request.put('/hr/transfer', data)
export const deleteTransfer = (id) => request.delete(`/hr/transfer/${id}`)
// 撤销调动
export const revokeTransfer = (id) => request.post(`/hr/transfer/${id}/revoke`)