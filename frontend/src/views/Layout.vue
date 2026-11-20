<template>
  <el-container class="layout">
    <!-- 左侧侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
      <!-- Logo 区 -->
      <div class="logo">
        <div class="logo-icon">E</div>
        <span v-show="!collapsed" class="logo-text">EOPMIS</span>
      </div>

      <!-- 菜单 -->
      <el-menu
        :default-active="$route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="#1e293b"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        router
        class="layout-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu v-if="hasPerm('sys:user:view') || hasPerm('sys:role:view')" index="sys">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="hasPerm('sys:user:view')" index="/admin/user">账号管理</el-menu-item>
          <el-menu-item v-if="hasPerm('sys:role:view')" index="/admin/role">角色管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu
          v-if="hasPerm('hr:dept:view') || hasPerm('hr:position:view') || hasPerm('hr:employee:view')
                || hasPerm('hr:contract:view') || hasPerm('hr:transfer:view')
                || hasPerm('hr:salary:view') || hasPerm('hr:attendance:view')"
          index="hr">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>人事业务</span>
          </template>
          <el-menu-item v-if="hasPerm('hr:dept:view')" index="/hr/dept">部门管理</el-menu-item>
          <el-menu-item v-if="hasPerm('hr:position:view')" index="/hr/position">岗位管理</el-menu-item>
          <el-menu-item v-if="hasPerm('hr:employee:view')" index="/hr/employee">职工档案</el-menu-item>
          <el-menu-item v-if="hasPerm('hr:contract:view')" index="/hr/contract">劳动合同</el-menu-item>
          <el-menu-item v-if="hasPerm('hr:transfer:view')" index="/hr/transfer">人事调动</el-menu-item>
          <el-menu-item v-if="hasPerm('hr:salary:view')" index="/hr/salary">薪资管理</el-menu-item>
          <el-menu-item v-if="hasPerm('hr:attendance:view')" index="/hr/attendance">考勤管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu
          v-if="hasPerm('me:pwd:update') || hasPerm('me:profile:view') || hasPerm('me:contract:view')
                || hasPerm('me:salary:view') || hasPerm('me:attendance:view')"
          index="me">
          <template #title>
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </template>
          <el-menu-item v-if="hasPerm('me:pwd:update')" index="/me/password">修改密码</el-menu-item>
          <el-menu-item v-if="hasPerm('me:profile:view')" index="/me/profile">我的档案</el-menu-item>
          <el-menu-item v-if="hasPerm('me:contract:view')" index="/me/contract">我的合同</el-menu-item>
          <el-menu-item v-if="hasPerm('me:salary:view')" index="/me/salary">我的薪资</el-menu-item>
          <el-menu-item v-if="hasPerm('me:attendance:view')" index="/me/attendance">我的考勤</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="collapsed = !collapsed">
        <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
      </div>
    </el-aside>

    <!-- 右侧主区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <span class="page-title">{{ $route.meta?.title || '首页' }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="onCmd">
            <div class="user-capsule">
              <div class="avatar">{{ avatarText }}</div>
              <div class="user-info">
                <div class="user-name">{{ userStore.realName }}</div>
                <div class="user-role">{{ userStore.roleName }}</div>
              </div>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const hasPerm = (p) => userStore.permissions.includes(p)

// 头像取姓名首字
const avatarText = computed(() => {
  const name = userStore.realName || userStore.username || 'U'
  return name.charAt(0).toUpperCase()
})

const onCmd = async (cmd) => {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
    await userStore.logoutAction()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

/* ===== 侧边栏 ===== */
.layout-aside {
  background: #1e293b;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}
.logo-icon {
  width: 34px;
  height: 34px;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}
.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
}

/* 菜单样式覆盖 */
.layout-menu {
  border-right: none !important;
  flex: 1;
  overflow-y: auto;
}
.layout-menu:not(.el-menu--collapse) {
  width: auto;
}
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 46px !important;
  line-height: 46px !important;
  margin: 2px 8px;
  border-radius: 6px;
}
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: #334155 !important;
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.25), rgba(37, 99, 235, 0.1)) !important;
  color: #fff !important;
  position: relative;
}
:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #3b82f6;
  border-radius: 0 3px 3px 0;
}
:deep(.el-sub-menu .el-menu-item) {
  min-width: auto !important;
  padding-left: 48px !important;
}

/* 折叠按钮 */
.collapse-btn {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  transition: color 0.2s;
  flex-shrink: 0;
}
.collapse-btn:hover {
  color: #fff;
}

/* ===== 顶部栏 ===== */
.layout-header {
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px !important;
  flex-shrink: 0;
  overflow: visible;  /* 关键：允许下拉菜单溢出显示 */
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

/* 用户胶囊 */
.user-capsule {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px 6px 6px;
  border-radius: 30px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}
.user-capsule:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}
.user-info {
  line-height: 1.2;
}
.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}
.user-role {
  font-size: 11px;
  color: #64748b;
}
.arrow {
  color: #94a3b8;
  font-size: 12px;
}

/* ===== 内容区 ===== */
.layout-main {
  background: #f0f4f8;
  padding: 20px;
  overflow-y: auto;
}
</style>
