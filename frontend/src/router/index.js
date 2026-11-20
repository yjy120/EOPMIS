import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'me/password', name: 'ChangePassword', component: () => import('@/views/ChangePassword.vue'), meta: { title: '修改密码' } },
      { path: 'admin/user', name: 'AccountManage', component: () => import('@/views/AccountManage.vue'), meta: { title: '账号管理', perm: 'sys:user:view' } },
      { path: 'admin/role', name: 'RoleManage', component: () => import('@/views/RoleManage.vue'), meta: { title: '角色管理', perm: 'sys:role:view' } },
      { path: 'hr/dept', name: 'DeptManage', component: () => import('@/views/DeptManage.vue'), meta: { title: '部门管理', perm: 'hr:dept:view' } },
      { path: 'hr/position', name: 'PositionManage', component: () => import('@/views/PositionManage.vue'), meta: { title: '岗位管理', perm: 'hr:position:view' } },
      { path: 'hr/employee', name: 'EmployeeManage', component: () => import('@/views/EmployeeManage.vue'), meta: { title: '职工档案', perm: 'hr:employee:view' } },
      { path: 'hr/contract', name: 'ContractManage', component: () => import('@/views/ContractManage.vue'), meta: { title: '劳动合同', perm: 'hr:contract:view' } },
      { path: 'hr/transfer', name: 'TransferManage', component: () => import('@/views/TransferManage.vue'), meta: { title: '人事调动', perm: 'hr:transfer:view' } },
      { path: 'hr/salary', name: 'SalaryManage', component: () => import('@/views/SalaryManage.vue'), meta: { title: '薪资管理', perm: 'hr:salary:view' } },
      { path: 'hr/attendance', name: 'AttendanceManage', component: () => import('@/views/AttendanceManage.vue'), meta: { title: '考勤管理', perm: 'hr:attendance:view' } },
      { path: 'me/profile', name: 'MyProfile', component: () => import('@/views/MyProfile.vue'), meta: { title: '我的档案', perm: 'me:profile:view' } },
      { path: 'me/contract', name: 'MyContract', component: () => import('@/views/MyContract.vue'), meta: { title: '我的合同', perm: 'me:contract:view' } },
      { path: 'me/salary', name: 'MySalary', component: () => import('@/views/MySalary.vue'), meta: { title: '我的薪资', perm: 'me:salary:view' } },
      { path: 'me/attendance', name: 'MyAttendance', component: () => import('@/views/MyAttendance.vue'), meta: { title: '我的考勤', perm: 'me:attendance:view' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') return next()
  if (!userStore.token) return next('/login')
  if (to.meta?.perm && !userStore.permissions.includes(to.meta.perm)) {
    // 简化处理：无权限跳首页
    return next('/dashboard')
  }
  next()
})

export default router
