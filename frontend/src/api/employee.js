import request from '@/utils/request'

// UC-007/008 职工档案管理 API
export const pageEmployees = (params) => request.get('/hr/employee/page', { params })
export const getEmployee = (id) => request.get(`/hr/employee/${id}`)
export const createEmployee = (data) => request.post('/hr/employee', data)
export const updateEmployee = (data) => request.put('/hr/employee', data)
export const deleteEmployee = (id) => request.delete(`/hr/employee/${id}`)