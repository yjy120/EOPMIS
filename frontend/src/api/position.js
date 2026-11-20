import request from '@/utils/request'

// UC-006 岗位管理 API
export const pagePositions = (params) => request.get('/hr/position/page', { params })
export const listPositionsByDept = (deptId) => request.get(`/hr/position/byDept/${deptId}`)
export const listAllPositions = async () => {
  // UC-007 复用：一次性拉全部岗位（最多 1000 条，足够小型企业）
  const data = await pagePositions({ pageNo: 1, pageSize: 1000 })
  return data.records || []
}
export const getPosition = (id) => request.get(`/hr/position/${id}`)
export const createPosition = (data) => request.post('/hr/position', data)
export const updatePosition = (data) => request.put('/hr/position', data)
export const deletePosition = (id) => request.delete(`/hr/position/${id}`)
