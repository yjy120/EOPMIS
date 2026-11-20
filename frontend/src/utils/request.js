import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers['Authorization'] = 'Bearer ' + userStore.token
  }
  return config
})

service.interceptors.response.use(
  resp => {
    const data = resp.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 0) {
        return data.data
      }
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message || 'fail'))
    }
    return data
  },
  err => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.clear()
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error('无访问权限')
    } else {
      ElMessage.error(msg || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default service
