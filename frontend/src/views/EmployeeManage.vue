<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>职工档案</span>
        <el-button v-if="hasPerm('hr:employee:add')" type="primary" :icon="Plus" @click="onCreate">新增职工</el-button>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="职工编号">
        <el-input v-model="query.empNo" placeholder="编号" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.name" placeholder="姓名" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="所属部门">
        <el-tree-select
          v-model="query.deptId"
          :data="deptTreeData"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          check-strictly
          clearable
          default-expand-all
          placeholder="全部"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item label="岗位">
        <el-select v-model="query.positionId" placeholder="全部" clearable filterable style="width: 180px">
          <el-option v-for="p in allPositions" :key="p.id" :label="`${p.name}（${deptNameOf(p.deptId)}）`" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="empNo" label="职工编号" width="110" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column label="性别" width="70">
        <template #default="{ row }">{{ row.genderLabel }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="联系方式" width="130" />
      <el-table-column prop="deptName" label="所属部门" width="130" />
      <el-table-column prop="positionName" label="岗位" width="120" />
      <el-table-column prop="level" label="职级" width="80" />
      <el-table-column prop="entryDate" label="入职日期" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPerm('hr:employee:edit')" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('hr:employee:detail')" size="small" @click="onView(row)">查看</el-button>
          <el-button v-if="hasPerm('hr:employee:delete')" size="small" type="danger" @click="onDelete(row)">删除</el-button>
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
      @size-change="onSearch"
      @current-change="onSearch"
    />

    <el-dialog v-model="dlgVisible" :title="dialogTitle" width="720" @closed="onClosed">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职工编号" prop="empNo">
              <el-input v-model="form.empNo" :disabled="readOnly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" :disabled="readOnly" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender" :disabled="readOnly">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" :disabled="readOnly" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" :disabled="readOnly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="phone">
              <el-input v-model="form.phone" :disabled="readOnly" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTreeData"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                check-strictly
                default-expand-all
                placeholder="请选择部门"
                style="width: 100%"
                :disabled="readOnly"
                @change="onDeptChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="positionId">
              <el-select v-model="form.positionId" placeholder="请选择岗位" filterable style="width: 100%" :disabled="readOnly">
                <el-option v-for="p in filteredPositions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职级">
              <el-input v-model="form.level" placeholder="如 P1/P2/P3" :disabled="readOnly" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryDate">
              <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" :disabled="readOnly" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status" :disabled="readOnly">
                <el-radio :value="1">在职</el-radio>
                <el-radio :value="0">离职</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绑定账号ID">
              <el-input-number v-model="form.userId" :min="0" :disabled="readOnly" style="width: 100%" />
              <span style="color: #999; font-size: 12px; margin-left: 8px">仅普通职工需绑定</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" :disabled="readOnly" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible = false">{{ readOnly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!readOnly" type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  pageEmployees, getEmployee, createEmployee, updateEmployee, deleteEmployee
} from '@/api/employee'
import { listAllDepts, deptTree } from '@/api/dept'
import { listAllPositions } from '@/api/position'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 20, empNo: '', name: '', deptId: null, positionId: null })

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
const deptNameOf = (id) => {
  const d = allDepts.value.find(x => x.id === id)
  return d ? d.name : ''
}

const allPositions = ref([])
const filteredPositions = computed(() => {
  if (!form.deptId) return allPositions.value
  return allPositions.value.filter(p => p.deptId === form.deptId)
})

const dlgVisible = ref(false)
const readOnly = ref(false)
const dialogTitle = computed(() => {
  if (readOnly.value) return '查看职工档案'
  return form.id ? '编辑职工档案' : '新增职工档案'
})
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  empNo: '',
  name: '',
  gender: 1,
  birthDate: null,
  idCard: '',
  phone: '',
  deptId: null,
  positionId: null,
  level: '',
  entryDate: null,
  status: 1,
  userId: null,
  remark: ''
})
const formRules = {
  empNo: [{ required: true, message: '请填写职工编号', trigger: 'blur' }],
  name: [{ required: true, message: '请填写姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  positionId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  entryDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }]
}

const onSearch = async () => {
  loading.value = true
  try {
    const data = await pageEmployees(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadAllDepts = async () => {
  allDepts.value = await listAllDepts()
}
const loadAllPositions = async () => {
  allPositions.value = await listAllPositions()
}

const onDeptChange = () => {
  // 切换部门后清空岗位（避免错位）
  form.positionId = null
}

const resetForm = () => {
  Object.assign(form, {
    id: null, empNo: '', name: '', gender: 1,
    birthDate: null, idCard: '', phone: '',
    deptId: null, positionId: null, level: '',
    entryDate: null, status: 1, userId: null, remark: ''
  })
}

const onCreate = () => {
  resetForm()
  readOnly.value = false
  dlgVisible.value = true
}

const fillForm = (row) => {
  Object.assign(form, {
    id: row.id,
    empNo: row.empNo,
    name: row.name,
    gender: row.gender,
    birthDate: row.birthDate,
    idCard: row.idCard,
    phone: row.phone,
    deptId: row.deptId,
    positionId: row.positionId,
    level: row.level,
    entryDate: row.entryDate,
    status: row.status,
    userId: row.userId,
    remark: row.remark
  })
}

const onEdit = async (row) => {
  const full = await getEmployee(row.id)
  fillForm(full)
  readOnly.value = false
  dlgVisible.value = true
}

const onView = async (row) => {
  const full = await getEmployee(row.id)
  fillForm(full)
  readOnly.value = true
  dlgVisible.value = true
}

const onClosed = () => {
  readOnly.value = false
}

const onSubmit = async () => {
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await updateEmployee(form)
      } else {
        await createEmployee(form)
      }
      ElMessage.success('保存成功')
      dlgVisible.value = false
      onSearch()
    } finally {
      saving.value = false
    }
  })
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除职工【${row.name}（${row.empNo}）】？`, '提示', { type: 'warning' })
  await deleteEmployee(row.id)
  ElMessage.success('已删除')
  onSearch()
}

onMounted(async () => {
  await Promise.all([loadAllDepts(), loadAllPositions()])
  onSearch()
})
</script>