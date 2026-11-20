import request from '@/utils/request'

// UC-009 普通职工本人档案
export const getMyProfile = () => request.get('/me/profile')