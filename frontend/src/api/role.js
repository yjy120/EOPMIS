import request from '@/utils/request'

export const listRoles = () => request.get('/admin/role')
export const getRole = (id) => request.get(`/admin/role/${id}`)
export const assignMenus = (data) => request.post('/admin/role/assign', data)
export const listAllMenus = () => request.get('/admin/role/menu/all')
