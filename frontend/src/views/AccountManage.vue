<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>账号管理</span>
        <el-button v-if="hasPerm('sys:user:add')" type="primary" :icon="Plus" @click="onCreate">新增账号</el-button>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="账号/姓名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option :value="1" label="正常" />
          <el-option :value="0" label="停用" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="username" label="账号" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="roleName" label="角色" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginAt" label="最后登录" width="170" />
      <el-table-column prop="lastLoginIp" label="IP" width="140" />
      <el-table-column label="操作" min-width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPerm('sys:user:enable') && row.status === 0" size="small" @click="onEnable(row)">启用</el-button>
          <el-button v-if="hasPerm('sys:user:enable') && row.status === 1" size="small" type="warning" @click="onDisable(row)">停用</el-button>
          <el-button v-if="hasPerm('sys:user:reset')" size="small" type="primary" @click="onReset(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 12px; text-align: right"
      v-model:current-page="query.pageNo"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
    />

    <!-- 新增弹窗 -->
    <el-dialog v-model="dlgVisible" title="新增账号" width="500">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :value="r.id" :label="r.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="form.initialPassword" placeholder="留空使用系统默认 Init@YOUR_DB_PASSWORD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { pageUsers, createUser, enableUser, disableUser, resetPassword } from '@/api/user'
import { listRoles } from '@/api/role'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 20, keyword: '', status: null })

const roles = ref([])
const dlgVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ username: '', realName: '', roleId: null, initialPassword: '' })
const formRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await pageUsers(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  roles.value = await listRoles()
}

const onCreate = () => {
  form.username = ''; form.realName = ''; form.roleId = null; form.initialPassword = ''
  dlgVisible.value = true
}

const onSubmit = async () => {
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      await createUser(form)
      ElMessage.success('创建成功')
      dlgVisible.value = false
      loadData()
    } finally {
      saving.value = false
    }
  })
}

const onEnable = async (row) => {
  await ElMessageBox.confirm(`确认启用账号 ${row.username}？`, '提示')
  await enableUser(row.id)
  ElMessage.success('已启用')
  loadData()
}
const onDisable = async (row) => {
  await ElMessageBox.confirm(`确认停用账号 ${row.username}？`, '提示', { type: 'warning' })
  await disableUser(row.id)
  ElMessage.success('已停用')
  loadData()
}
const onReset = async (row) => {
  await ElMessageBox.confirm(`将 ${row.username} 密码重置为系统默认？`, '提示', { type: 'warning' })
  await resetPassword(row.id, {})
  ElMessage.success('密码已重置')
}

onMounted(() => { loadRoles(); loadData() })
</script>
