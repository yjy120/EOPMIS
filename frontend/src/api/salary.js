import request from '@/utils/request'

// UC-014 薪资管理 HR 端 API
export const pageSalary = (params) => request.get('/hr/salary/page', { params })
export const getSalary = (id) => request.get(`/hr/salary/${id}`)
export const createSalary = (data) => request.post('/hr/salary', data)
export const updateSalary = (data) => request.put('/hr/salary', data)
export const deleteSalary = (id) => request.delete(`/hr/salary/${id}`)

// UC-016 普通职工本人薪资
export const getMySalary = () => request.get('/me/salary')