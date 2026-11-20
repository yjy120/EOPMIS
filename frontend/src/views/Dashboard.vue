<template>
  <div class="dashboard">
    <!-- ============ 系统管理员首页 ============ -->
    <template v-if="roleCode === 'ADMIN'">
      <!-- 欢迎区 -->
      <div class="welcome-card">
        <div class="welcome-left">
          <div class="welcome-title">您好，系统管理员</div>
          <div class="welcome-sub">{{ userStore.username }} · {{ userStore.roleName }} · 欢迎使用企业职工人事在线管理信息系统</div>
        </div>
        <div class="welcome-icon">
          <el-icon :size="48" color="#fff"><Setting /></el-icon>
        </div>
      </div>

      <!-- 统计概览 -->
      <div class="stat-row">
        <div class="stat-card" v-for="s in adminStats" :key="s.label">
          <div class="stat-icon" :style="{ background: s.bg }">
            <el-icon :size="22" :color="s.color"><component :is="s.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="section-title">快捷入口</div>
      <div class="quick-row">
        <div class="quick-card" v-for="q in adminQuick" :key="q.path" @click="go(q.path)">
          <div class="quick-icon" :style="{ background: q.bg }">
            <el-icon :size="22" color="#fff"><component :is="q.icon" /></el-icon>
          </div>
          <div class="quick-name">{{ q.name }}</div>
        </div>
      </div>

      <!-- 最近审计日志 -->
      <div class="section-title">最近审计日志</div>
      <div class="app-card">
        <el-table :data="recentAudits" stripe style="width: 100%" empty-text="暂无日志">
          <el-table-column prop="createTime" label="时间" width="170" />
          <el-table-column prop="username" label="操作人" width="120" />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="action" label="操作" width="140" />
          <el-table-column prop="detail" label="详情" show-overflow-tooltip />
          <el-table-column label="结果" width="80">
            <template #default="{ row }">
              <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">
                {{ row.result === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <!-- ============ 人事专员首页 ============ -->
    <template v-else-if="roleCode === 'HR'">
      <!-- 欢迎区 -->
      <div class="welcome-card">
        <div class="welcome-left">
          <div class="welcome-title">您好，人事专员</div>
          <div class="welcome-sub">{{ userStore.username }} · {{ userStore.roleName }} · 高效管理企业人事事务</div>
        </div>
        <div class="welcome-icon">
          <el-icon :size="48" color="#fff"><OfficeBuilding /></el-icon>
        </div>
      </div>

      <!-- 统计概览 6 张 -->
      <div class="stat-row">
        <div class="stat-card" v-for="s in hrStats" :key="s.label">
          <div class="stat-icon" :style="{ background: s.bg }">
            <el-icon :size="22" :color="s.color"><component :is="s.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="section-title">快捷入口</div>
      <div class="quick-row">
        <div class="quick-card" v-for="q in hrQuick" :key="q.path" @click="go(q.path)">
          <div class="quick-icon" :style="{ background: q.bg }">
            <el-icon :size="22" color="#fff"><component :is="q.icon" /></el-icon>
          </div>
          <div class="quick-name">{{ q.name }}</div>
        </div>
      </div>

      <!-- 图表区：左柱图 + 右预警 -->
      <div class="chart-row">
        <div class="app-card chart-card">
          <div class="card-header">各部门职工人数</div>
          <v-chart :option="deptChartOption" autoresize style="height: 320px" />
        </div>
        <div class="app-card chart-card">
          <div class="card-header">合同到期预警</div>
          <el-table :data="expiringContracts" stripe size="small" empty-text="暂无到期合同">
            <el-table-column prop="employeeName" label="职工" width="90" />
            <el-table-column prop="contractNo" label="合同编号" width="130" show-overflow-tooltip />
            <el-table-column prop="endDate" label="到期日" width="110" />
            <el-table-column label="剩余" width="70">
              <template #default="{ row }">
                <span :style="{ color: row.days <= 30 ? '#ef4444' : '#f59e0b' }">{{ row.days }}天</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>

    <!-- ============ 普通职工首页 ============ -->
    <template v-else>
      <!-- 欢迎区 -->
      <div class="welcome-card">
        <div class="welcome-left">
          <div class="welcome-title">您好，{{ myProfile?.name || userStore.realName }}</div>
          <div class="welcome-sub">
            {{ myProfile?.deptName || '—' }} · {{ myProfile?.positionName || '—' }}
            <span v-if="myProfile?.empNo"> · 工号 {{ myProfile.empNo }}</span>
          </div>
        </div>
        <div class="welcome-icon">
          <el-icon :size="48" color="#fff"><User /></el-icon>
        </div>
      </div>

      <!-- 统计概览 4 张 -->
      <div class="stat-row">
        <div class="stat-card" v-for="s in empStats" :key="s.label">
          <div class="stat-icon" :style="{ background: s.bg }">
            <el-icon :size="22" :color="s.color"><component :is="s.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="section-title">快捷入口</div>
      <div class="quick-row">
        <div class="quick-card" v-for="q in empQuick" :key="q.path" @click="go(q.path)">
          <div class="quick-icon" :style="{ background: q.bg }">
            <el-icon :size="22" color="#fff"><component :is="q.icon" /></el-icon>
          </div>
          <div class="quick-name">{{ q.name }}</div>
        </div>
      </div>

      <!-- 底部：本月考勤 + 合同状态 -->
      <div class="chart-row">
        <div class="app-card chart-card">
          <div class="card-header">本月考勤记录</div>
          <el-table :data="myAttendanceList" stripe size="small" empty-text="暂无考勤记录">
            <el-table-column prop="attendanceDate" label="日期" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="attStatusType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="leaveType" label="请假类型" width="100" />
            <el-table-column prop="overtimeHours" label="加班(h)" width="80" />
            <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          </el-table>
        </div>
        <div class="app-card chart-card">
          <div class="card-header">我的合同状态</div>
          <div v-if="myContractList.length" class="contract-info">
            <div class="contract-item" v-for="c in myContractList" :key="c.id">
              <div class="contract-no">{{ c.contractNo }}</div>
              <div class="contract-meta">
                <span>{{ c.contractType }}</span>
                <span>{{ c.startDate }} ~ {{ c.endDate }}</span>
              </div>
              <el-tag :type="contractStatusType(c.status)" size="small">{{ c.statusLabel }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无合同信息" :image-size="80" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'

import { pageUsers } from '@/api/user'
import { listRoles } from '@/api/role'
import { pageAuditLogs, countAuditLogs } from '@/api/audit'
import { pageEmployees } from '@/api/employee'
import { listAllDepts } from '@/api/dept'
import { listAllPositions } from '@/api/position'
import { pageContracts } from '@/api/contract'
import { pageSalary } from '@/api/salary'
import { pageAttendance } from '@/api/attendance'
import { getMyProfile } from '@/api/me'
import { getMyContracts } from '@/api/contract'
import { getMySalary } from '@/api/salary'
import { getMyAttendance } from '@/api/attendance'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent])

const router = useRouter()
const userStore = useUserStore()
const roleCode = computed(() => userStore.roleCode)

const go = (path) => router.push(path)

// ============ 通用：考勤状态颜色 ============
const attStatusType = (s) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
  return map[s] || 'info'
}
const contractStatusType = (s) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
  return map[s] || 'info'
}

// ============ ADMIN 数据 ============
const adminStats = ref([
  { label: '系统总用户数', value: 0, icon: 'User', color: '#2563eb', bg: '#eff6ff' },
  { label: '启用账号数', value: 0, icon: 'CircleCheck', color: '#16a34a', bg: '#f0fdf4' },
  { label: '角色总数', value: 0, icon: 'UserFilled', color: '#9333ea', bg: '#faf5ff' },
  { label: '审计日志总数', value: 0, icon: 'Document', color: '#ea580c', bg: '#fff7ed' }
])
const adminQuick = [
  { name: '账号管理', path: '/admin/user', icon: 'User', bg: '#2563eb' },
  { name: '角色管理', path: '/admin/role', icon: 'UserFilled', bg: '#7c3aed' },
  { name: '个人中心', path: '/me/profile', icon: 'User', bg: '#0891b2' }
]
const recentAudits = ref([])

const loadAdmin = async () => {
  try {
    const [users, roles, auditCount] = await Promise.all([
      pageUsers({ pageNo: 1, pageSize: 1000 }),
      listRoles(),
      countAuditLogs().catch(() => 0)
    ])
    adminStats.value[0].value = users.total || 0
    adminStats.value[1].value = (users.records || []).filter(u => u.status === 1).length
    adminStats.value[2].value = (roles || []).length
    adminStats.value[3].value = auditCount || 0
  } catch (e) { /* 接口异常不阻塞展示 */ }
  try {
    const pg = await pageAuditLogs({ pageNo: 1, pageSize: 10 })
    recentAudits.value = pg.records || []
  } catch (e) { recentAudits.value = [] }
}

// ============ HR 数据 ============
const hrStats = ref([
  { label: '在职职工总数', value: 0, icon: 'User', color: '#2563eb', bg: '#eff6ff' },
  { label: '部门总数', value: 0, icon: 'OfficeBuilding', color: '#16a34a', bg: '#f0fdf4' },
  { label: '岗位总数', value: 0, icon: 'Briefcase', color: '#9333ea', bg: '#faf5ff' },
  { label: '有效劳动合同', value: 0, icon: 'Document', color: '#0891b2', bg: '#ecfeff' },
  { label: '本月薪资记录', value: 0, icon: 'Money', color: '#ca8a04', bg: '#fefce8' },
  { label: '本月考勤记录', value: 0, icon: 'Calendar', color: '#ea580c', bg: '#fff7ed' }
])
const hrQuick = [
  { name: '部门管理', path: '/hr/dept', icon: 'OfficeBuilding', bg: '#2563eb' },
  { name: '岗位管理', path: '/hr/position', icon: 'Briefcase', bg: '#7c3aed' },
  { name: '职工档案', path: '/hr/employee', icon: 'User', bg: '#0891b2' },
  { name: '劳动合同', path: '/hr/contract', icon: 'Document', bg: '#16a34a' },
  { name: '人事调动', path: '/hr/transfer', icon: 'Switch', bg: '#ca8a04' },
  { name: '薪资管理', path: '/hr/salary', icon: 'Money', bg: '#dc2626' },
  { name: '考勤管理', path: '/hr/attendance', icon: 'Calendar', bg: '#ea580c' },
  { name: '个人中心', path: '/me/profile', icon: 'User', bg: '#64748b' }
]
const expiringContracts = ref([])
const deptChartOption = ref({})

const loadHr = async () => {
  try {
    const [emps, depts, positions, contracts] = await Promise.all([
      pageEmployees({ pageNo: 1, pageSize: 1000 }),
      listAllDepts(),
      listAllPositions(),
      pageContracts({ pageNo: 1, pageSize: 1000 })
    ])
    const empList = emps.records || []
    hrStats.value[0].value = empList.filter(e => e.status === 1).length
    hrStats.value[1].value = (depts || []).length
    hrStats.value[2].value = (positions || []).length

    const contractList = contracts.records || []
    hrStats.value[3].value = contractList.filter(c => c.status === 1).length

    // 本月薪资/考勤记录数
    const now = new Date()
    const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    try {
      const sal = await pageSalary({ pageNo: 1, pageSize: 1000 })
      hrStats.value[4].value = (sal.records || []).filter(s => s.salaryMonth === ym).length
    } catch (e) { hrStats.value[4].value = 0 }
    try {
      const att = await pageAttendance({ pageNo: 1, pageSize: 1000 })
      hrStats.value[5].value = (att.records || []).filter(a => {
        return a.attendanceDate && a.attendanceDate.startsWith(ym)
      }).length
    } catch (e) { hrStats.value[5].value = 0 }

    // 部门人数柱图
    const deptMap = {}
    empList.forEach(e => {
      if (e.deptName) deptMap[e.deptName] = (deptMap[e.deptName] || 0) + 1
    })
    const names = Object.keys(deptMap)
    deptChartOption.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: names, axisLabel: { color: '#64748b', fontSize: 11 } },
      yAxis: { type: 'value', axisLabel: { color: '#64748b' }, splitLine: { lineStyle: { color: '#f1f5f9' } } },
      series: [{
        type: 'bar',
        data: names.map(n => deptMap[n]),
        itemStyle: { color: '#2563eb', borderRadius: [4, 4, 0, 0] },
        barWidth: 28
      }]
    }

    // 合同到期预警（60天内）
    const today = new Date()
    expiringContracts.value = contractList
      .filter(c => c.status === 1 && c.endDate)
      .map(c => {
        const days = Math.ceil((new Date(c.endDate) - today) / 86400000)
        return { ...c, days }
      })
      .filter(c => c.days <= 60 && c.days >= -30)
      .sort((a, b) => a.days - b.days)
      .slice(0, 8)
  } catch (e) { /* 不阻塞 */ }
}

// ============ EMPLOYEE 数据 ============
const myProfile = ref(null)
const myContractList = ref([])
const myAttendanceList = ref([])
const empStats = ref([
  { label: '入职时长', value: '—', icon: 'Calendar', color: '#2563eb', bg: '#eff6ff' },
  { label: '合同剩余期限', value: '—', icon: 'Document', color: '#16a34a', bg: '#f0fdf4' },
  { label: '上月实发薪资', value: '—', icon: 'Money', color: '#ca8a04', bg: '#fefce8' },
  { label: '本月出勤天数', value: '—', icon: 'Clock', color: '#ea580c', bg: '#fff7ed' }
])
const empQuick = [
  { name: '我的资料', path: '/me/profile', icon: 'User', bg: '#2563eb' },
  { name: '我的合同', path: '/me/contract', icon: 'Document', bg: '#16a34a' },
  { name: '我的薪资', path: '/me/salary', icon: 'Money', bg: '#ca8a04' },
  { name: '我的考勤', path: '/me/attendance', icon: 'Calendar', bg: '#ea580c' },
  { name: '修改密码', path: '/me/password', icon: 'Lock', bg: '#64748b' }
]

const loadEmployee = async () => {
  try {
    myProfile.value = await getMyProfile()
    // 入职时长
    if (myProfile.value.entryDate) {
      const days = Math.floor((Date.now() - new Date(myProfile.value.entryDate)) / 86400000)
      empStats.value[0].value = days >= 365 ? `${(days / 365).toFixed(1)}年` : `${days}天`
    }
  } catch (e) { myProfile.value = null }

  try {
    myContractList.value = await getMyContracts()
    const active = myContractList.value.find(c => c.status === 1)
    if (active && active.endDate) {
      const days = Math.ceil((new Date(active.endDate) - Date.now()) / 86400000)
      empStats.value[1].value = days > 0 ? `${days}天` : '已到期'
    } else {
      empStats.value[1].value = '无有效合同'
    }
  } catch (e) { myContractList.value = [] }

  try {
    const salaries = await getMySalary()
    if (salaries && salaries.length) {
      const last = salaries[0]
      empStats.value[2].value = `¥${Number(last.actualSalary || 0).toLocaleString()}`
    }
  } catch (e) { /* 无薪资数据 */ }

  try {
    const now = new Date()
    const start = new Date(now.getFullYear(), now.getMonth(), 1)
    const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    const fmt = d => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    myAttendanceList.value = await getMyAttendance({ startDate: fmt(start), endDate: fmt(end) })
    empStats.value[3].value = myAttendanceList.value.filter(a => a.status === 1).length
  } catch (e) { myAttendanceList.value = [] }
}

onMounted(() => {
  if (roleCode.value === 'ADMIN') loadAdmin()
  else if (roleCode.value === 'HR') loadHr()
  else loadEmployee()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 欢迎区 */
.welcome-card {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
  border-radius: 12px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.25);
}
.welcome-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
}
.welcome-sub {
  font-size: 13px;
  opacity: 0.85;
}
.welcome-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 统计卡片 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #f1f5f9;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
  transition: all 0.2s;
}
.stat-card:hover {
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  transform: translateY(-1px);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

/* 区块标题 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 4px 2px 0;
}

/* 快捷入口 */
.quick-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 14px;
}
.quick-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #f1f5f9;
  padding: 18px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
}
.quick-card:hover {
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  transform: translateY(-2px);
}
.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.quick-name {
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

/* 图表区 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.chart-card {
  padding: 16px 20px;
}
.card-header {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
}

/* 合同信息 */
.contract-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.contract-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
}
.contract-no {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}
.contract-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #64748b;
}

@media (max-width: 900px) {
  .chart-row { grid-template-columns: 1fr; }
}
</style>
