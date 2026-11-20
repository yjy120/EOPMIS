<template>
  <el-card>
    <template #header>
      <span>角色管理</span>
    </template>

    <el-table :data="roles" v-loading="loading" border @current-change="onSelect" highlight-current-row>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="角色编码" width="160" />
      <el-table-column prop="name" label="角色名称" width="160" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.isSystem === 1" type="danger">系统内置</el-tag>
          <el-tag v-else type="info">自定义</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-divider />

    <div v-if="current" style="display: flex; justify-content: space-between; align-items: center">
      <h3>为角色【{{ current.name }}】分配权限</h3>
      <div>
        <el-button @click="onCheckAll">全选</el-button>
        <el-button @click="onUncheckAll">全不选</el-button>
        <el-button type="primary" :loading="saving" :disabled="!hasPerm('sys:role:assign')" @click="onSave">保存</el-button>
      </div>
    </div>

    <el-tree
      v-if="current"
      ref="treeRef"
      :data="menuTree"
      :props="{ label: 'name', children: 'children' }"
      show-checkbox
      node-key="id"
      :default-checked-keys="current.menuIds"
      :default-expand-all="true"
      style="margin-top: 12px"
    />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { listRoles, listAllMenus, assignMenus } from '@/api/role'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const roles = ref([])
const allMenus = ref([])
const current = ref(null)
const loading = ref(false)
const saving = ref(false)
const treeRef = ref()

const menuTree = computed(() => {
  // 把扁平菜单构造为树
  const map = new Map()
  const roots = []
  for (const m of allMenus.value) {
    map.set(m.id, { ...m, children: [] })
  }
  for (const m of allMenus.value) {
    const node = map.get(m.id)
    if (m.parentId === 0) {
      roots.push(node)
    } else if (map.has(m.parentId)) {
      map.get(m.parentId).children.push(node)
    } else {
      roots.push(node)
    }
  }
  return roots
})

const loadData = async () => {
  loading.value = true
  try {
    roles.value = await listRoles()
    allMenus.value = await listAllMenus()
  } finally {
    loading.value = false
  }
}

const onSelect = async (row) => {
  current.value = row
  await nextTick()
  // 重新设置勾选
  treeRef.value?.setCheckedKeys(row.menuIds || [])
}

const onCheckAll = () => {
  const ids = allMenus.value.map(m => m.id)
  treeRef.value?.setCheckedKeys(ids)
}
const onUncheckAll = () => treeRef.value?.setCheckedKeys([])

const onSave = async () => {
  const checked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  // 同时保存父级和子级，避免后端只收子级时丢失路径
  const all = [...new Set([...checked, ...halfChecked])]
  saving.value = true
  try {
    await assignMenus({ roleId: current.value.id, menuIds: all })
    ElMessage.success('保存成功')
    loadData()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>
