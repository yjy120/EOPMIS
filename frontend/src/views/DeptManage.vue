<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>部门管理</span>
        <el-button v-if="hasPerm('hr:dept:add')" type="primary" :icon="Plus" @click="onCreate">新增部门</el-button>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="名称">
        <el-input v-model="query.name" placeholder="部门名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="上级部门">
        <el-tree-select
          v-model="query.parentId"
          :data="deptTreeData"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          check-strictly
          clearable
          default-expand-all
          placeholder="全部"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="部门名称" />
      <el-table-column prop="code" label="编码" width="120" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="parentId" label="上级ID" width="100" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPerm('hr:dept:edit')" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('hr:dept:delete')" size="small" type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dlgVisible" :title="form.id ? '编辑部门' : '新增部门'" width="500">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            default-expand-all
            placeholder="顶级"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  pageDepts, listAllDepts, createDept, updateDept, deleteDept
} from '@/api/dept'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 20, name: '', parentId: null })

const allDepts = ref([])
const deptTreeData = computed(() => {
  const map = new Map()
  const roots = []
  for (const d of allDepts.value) {
    map.set(d.id, { ...d, children: [] })
  }
  for (const d of allDepts.value) {
    const node = map.get(d.id)
    if (!d.parentId || d.parentId === 0) {
      roots.push(node)
    } else if (map.has(d.parentId)) {
      map.get(d.parentId).children.push(node)
    } else {
      roots.push(node)
    }
  }
  return roots
})

const dlgVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null, parentId: 0, name: '', code: '', leader: '', sort: 0, status: 1
})
const formRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await pageDepts(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadAllDepts = async () => {
  allDepts.value = await listAllDepts()
}

const onCreate = () => {
  form.id = null
  form.parentId = 0
  form.name = ''
  form.code = ''
  form.leader = ''
  form.sort = 0
  form.status = 1
  dlgVisible.value = true
}

const onEdit = (row) => {
  form.id = row.id
  form.parentId = row.parentId
  form.name = row.name
  form.code = row.code
  form.leader = row.leader
  form.sort = row.sort
  form.status = row.status
  dlgVisible.value = true
}

const onSubmit = async () => {
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await updateDept(form)
      } else {
        await createDept(form)
      }
      ElMessage.success('保存成功')
      dlgVisible.value = false
      await loadAllDepts()
      loadData()
    } finally {
      saving.value = false
    }
  })
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除部门【${row.name}】？`, '提示', { type: 'warning' })
  await deleteDept(row.id)
  ElMessage.success('已删除')
  await loadAllDepts()
  loadData()
}

onMounted(() => { loadAllDepts(); loadData() })
</script>
