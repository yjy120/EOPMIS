<template>
  <div class="login-bg">
    <div class="login-card">
      <h2>EOPMIS</h2>
      <div class="subtitle">企业职工人事在线管理信息系统</div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password @keyup.enter="onLogin" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onLogin">登录</el-button>
      </el-form>

      <div style="margin-top: 16px; color: #909399; font-size: 12px; text-align: center;">
        测试账号：admin / YOUR_TEST_PWD · hr01 / YOUR_HR_PWD · emp01 / YOUR_EMP_PWD
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'YOUR_TEST_PWD' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onLogin = async () => {
  await formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.loginAction({ username: form.username, password: form.password })
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (e) {
      // 已由 request.js 弹窗
    } finally {
      loading.value = false
    }
  })
}
</script>
