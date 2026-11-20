import { defineStore } from 'pinia'
import { login, logout } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: Number(localStorage.getItem('userId')) || 0,
    username: localStorage.getItem('username') || '',
    realName: localStorage.getItem('realName') || '',
    roleCode: localStorage.getItem('roleCode') || '',
    roleName: localStorage.getItem('roleName') || '',
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
    menus: JSON.parse(localStorage.getItem('menus') || '[]')
  }),

  actions: {
    async loginAction(req) {
      const data = await login(req)
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.roleCode = data.roleCode
      this.roleName = data.roleName
      this.permissions = data.permissions || []
      this.menus = data.menus || []
      localStorage.setItem('token', this.token)
      localStorage.setItem('userId', this.userId)
      localStorage.setItem('username', this.username)
      localStorage.setItem('realName', this.realName)
      localStorage.setItem('roleCode', this.roleCode)
      localStorage.setItem('roleName', this.roleName)
      localStorage.setItem('permissions', JSON.stringify(this.permissions))
      localStorage.setItem('menus', JSON.stringify(this.menus))
      return data
    },

    async logoutAction() {
      try { await logout() } catch (e) {}
      this.clear()
    },

    clear() {
      this.token = ''
      this.userId = 0
      this.username = ''
      this.realName = ''
      this.roleCode = ''
      this.roleName = ''
      this.permissions = []
      this.menus = []
      localStorage.clear()
    }
  }
})
