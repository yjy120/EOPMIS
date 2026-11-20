import request from '@/utils/request'

// UC-005 部门管理 API
export const pageDepts = (params) => request.get('/hr/dept/page', { params })
export const listAllDepts = () => request.get('/hr/dept/all')
export const deptTree = () => request.get('/hr/dept/tree')
export const getDept = (id) => request.get(`/hr/dept/${id}`)
export const createDept = (data) => request.post('/hr/dept', data)
export const updateDept = (data) => request.put('/hr/dept', data)
export const deleteDept = (id) => request.delete(`/hr/dept/${id}`)
