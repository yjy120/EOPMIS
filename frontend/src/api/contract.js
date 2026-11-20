import request from '@/utils/request'

// UC-010/011 劳动合同 HR 端 API
export const pageContracts = (params) => request.get('/hr/contract/page', { params })
export const getContract = (id) => request.get(`/hr/contract/${id}`)
export const createContract = (data) => request.post('/hr/contract', data)
export const updateContract = (data) => request.put('/hr/contract', data)
export const deleteContract = (id) => request.delete(`/hr/contract/${id}`)
// UC-011 续签：路径参数 id = 原合同 id
export const renewContract = (id, data) => request.post(`/hr/contract/${id}/renew`, data)
// UC-011 终止：路径参数 id = 被终止合同 id
export const terminateContract = (id, data) => request.post(`/hr/contract/${id}/terminate`, data)

// UC-012 普通职工本人合同
export const getMyContracts = () => request.get('/me/contract')