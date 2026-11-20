import request from '@/utils/request'

export const pageUsers = (params) => request.get('/admin/user/page', { params })
export const createUser = (data) => request.post('/admin/user', data)
export const enableUser = (id) => request.post(`/admin/user/${id}/enable`)
export const disableUser = (id) => request.post(`/admin/user/${id}/disable`)
export const resetPassword = (id, data) => request.post(`/admin/user/${id}/resetPassword`, data || {})
