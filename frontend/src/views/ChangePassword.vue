<template>
  <el-card>
    <template #header><span>修改个人密码</span></template>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 500px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
        <div style="color: #909399; font-size: 12px; margin-top: 4px">
          至少 8 位，且包含字母与数字；不能与最近 3 次相同
        </div>
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '至少 8 位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: '必须包含字母与数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (rule, value, cb) => value === form.newPassword ? cb() : cb(new Error('两次输入不一致')), trigger: 'blur' }
  ]
}

const onSubmit = async () => {
  await formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await changePassword(form)
      ElMessage.success('密码修改成功，请重新登录')
      await userStore.logoutAction()
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}
</script>
