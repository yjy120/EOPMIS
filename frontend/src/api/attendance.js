import request from '@/utils/request'

// UC-015 考勤管理 HR 端 API
export const pageAttendance = (params) => request.get('/hr/attendance/page', { params })
export const getAttendance = (id) => request.get(`/hr/attendance/${id}`)
export const createAttendance = (data) => request.post('/hr/attendance', data)
export const updateAttendance = (data) => request.put('/hr/attendance', data)
export const deleteAttendance = (id) => request.delete(`/hr/attendance/${id}`)

// UC-017 普通职工本人考勤
export const getMyAttendance = (params) => request.get('/me/attendance', { params })