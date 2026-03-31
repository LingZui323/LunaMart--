<template>
  <div class="login-page">
    <van-nav-bar title="登录" fixed :placeholder="true" class="moon-nav-bar" />

    <div class="form-wrapper">
      <van-cell-group inset class="form-group">
        <van-field v-model="form.account" placeholder="请输入用户名/邮箱" label="账号" />
        <van-field v-model="form.password" placeholder="请输入密码" label="密码" type="password" />
      </van-cell-group>

      <van-button
        type="primary"
        color="#FFE181"
        class="login-btn"
        @click="handleLogin"
        :loading="loading"
      >
        登录
      </van-button>

      <div class="to-register" @click="$router.push('/register')">
        没有账号？立即注册
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '../../store/user'

const router = useRouter()
const loading = ref(false)

const form = ref({
  account: '',
  password: ''
})

// 延迟获取 userStore，解决 Pinia 未激活问题
let userStore = null
onMounted(() => {
  userStore = useUserStore()
})

const handleLogin = async () => {
  if (!form.value.account || !form.value.password) {
    showToast('请输入账号和密码')
    return
  }

  try {
    loading.value = true
    const params = new URLSearchParams()
    params.append('account', form.value.account)
    params.append('password', form.value.password)

    const res = await fetch('/api/user/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: params
    })

    const data = await res.json()
    console.log('登录返回:', data)

    if (data.code === 200) {
      userStore.login({
        ...data.data.user,
        token: data.data.token
      })
      
      showToast('登录成功')
      router.push('/profile')
    } else {
      showToast(data.message || '账号或密码错误')
    }
  } catch (e) {
    console.error(e)
    showToast('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  background: #fff9e6;
  min-height: 100vh;
}
.moon-nav-bar {
  background: #FFE181;
}
.form-wrapper {
  padding: 30px 15px;
}
.form-group {
  border-radius: 12px;
  margin-bottom: 25px;
}
.login-btn {
  width: 100%;
  border-radius: 12px;
  color: #333;
  font-weight: 500;
}
.to-register {
  text-align: center;
  color: #ff9f00;
  margin-top: 15px;
  font-size: 14px;
}
</style>