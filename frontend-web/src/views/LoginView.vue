<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { fetchCaptcha } from '@/api/adapters/auth'
import UiSpinner from '@/components/common/UiSpinner.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  userName: '',
  password: '',
  captchaValue: '',
  captchaKey: ''
})

const captchaImage = ref('')

const loadCaptcha = async () => {
  try {
    const data = await fetchCaptcha()
    form.captchaKey = data.captchaKey
    captchaImage.value = data.imageBase64
  } catch (error) {
    errorMessage.value = (error as Error).message || '验证码加载失败'
  }
}

const submit = async () => {
  errorMessage.value = ''
  loading.value = true
  try {
    await authStore.login(form)
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } catch (error) {
    errorMessage.value = (error as Error).message || '登录失败'
    await loadCaptcha()
    form.captchaValue = ''
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<template>
  <div class="login-page">
    <form class="login-card" @submit.prevent="submit">
      <h1>能耗预付费管理系统</h1>

      <label class="field">
        <span>用户名</span>
        <input v-model="form.userName" type="text" autocomplete="username" required />
      </label>

      <label class="field">
        <span>密码</span>
        <input v-model="form.password" type="password" autocomplete="current-password" required />
      </label>

      <label class="field">
        <span>验证码</span>
        <input v-model="form.captchaValue" type="text" required />
      </label>

      <button
        v-if="captchaImage"
        type="button"
        class="captcha-wrap"
        title="点击刷新验证码"
        @click="loadCaptcha"
      >
        <img :src="captchaImage" alt="captcha" />
      </button>
      <div v-else class="captcha-wrap-placeholder">
        <UiSpinner :size="18" :thickness="2" color="#64748b" />
      </div>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button type="submit" class="submit-btn" :disabled="loading"> 登录 </button>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0f172a 0%, #1f2937 50%, #334155 100%);
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 380px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgb(15 23 42 / 22%);
}

h1 {
  margin: 0 0 18px;
  font-size: 22px;
  color: #111827;
}

.field {
  display: block;
  margin-bottom: 14px;
}

.field span {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #374151;
}

.field input {
  width: 100%;
  height: 38px;
  padding: 0 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.captcha-wrap,
.captcha-wrap-placeholder {
  display: flex;
  width: 100%;
  height: 48px;
  margin-bottom: 14px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  align-items: center;
  justify-content: center;
}

.captcha-wrap {
  padding: 0;
}

.captcha-wrap img {
  max-height: 42px;
}

.error {
  margin: 0 0 10px;
  font-size: 13px;
  color: #dc2626;
}

.submit-btn {
  display: inline-flex;
  width: 100%;
  height: 40px;
  font-weight: 600;
  color: #fff;
  background: #2563eb;
  border: none;
  border-radius: 8px;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.submit-btn:disabled {
  opacity: 0.6;
}
</style>
