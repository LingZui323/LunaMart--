<template>
  <div class="register-page">
    <van-nav-bar title="注册" fixed :placeholder="true" class="moon-nav-bar" />

    <div class="form-wrapper">
      <van-cell-group inset class="form-group">
        <van-field v-model="form.username" placeholder="用户名" label="用户名" />
        <van-field v-model="form.email" placeholder="QQ邮箱" label="邮箱" type="email" />

        <van-field
          v-model="form.code"
          placeholder="请输入邮箱验证码"
          label="验证码"
          clearable
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              plain
              @click="sendCode"
              :disabled="codeDisabled"
            >
              {{ codeText }}
            </van-button>
          </template>
        </van-field>

        <van-field v-model="form.password" placeholder="密码" label="密码" type="password" />
      </van-cell-group>

      <van-button
        type="primary"
        color="#FFE181"
        class="register-btn"
        @click="handleRegister"
        :loading="loading"
      >
        注册
      </van-button>

      <div class="to-login" @click="$router.push('/login')">
        已有账号？立即登录
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()
const loading = ref(false)

const form = ref({
  username: '',
  email: '',
  password: '',
  code: ''
})

// 验证码倒计时
const codeDisabled = ref(false)
const codeText = ref('获取验证码')
let countdownTimer = null

// ==========================================
// 发送验证码（GET + @RequestParam）
// ==========================================
const sendCode = async () => {
  const email = form.value.email
  if (!email) {
    showToast('请输入邮箱')
    return
  }

  try {
    const res = await fetch(`/api/user/sendCode?email=${email}`)
    const data = await res.json()

    if (data.code === 200) {
      showToast('验证码发送成功')
      codeDisabled.value = true
      let sec = 60
      codeText.value = sec + '秒后重发'
      const timer = setInterval(() => {
        sec--
        codeText.value = sec + '秒后重发'
        if (sec <= 0) {
          clearInterval(timer)
          codeDisabled.value = false
          codeText.value = '获取验证码'
        }
      }, 1000)
    } else {
      showToast(data.message || '发送失败')
    }
  } catch (e) {
    showToast('发送失败')
  }
}

// ==========================================
// 注册（POST + @RequestParam 表单格式）
// ==========================================
const handleRegister = async () => {
  const { username, email, code, password } = form.value
  if (!username || !email || !code || !password) {
    showToast('请填写完整信息')
    return
  }

  try {
    loading.value = true

    // ✅ 关键：严格匹配后端 @RequestParam 表单格式
    const params = new URLSearchParams()
    params.append('username', username)
    params.append('email', email)
    params.append('code', code)
    params.append('password', password)

    const res = await fetch('/api/user/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: params
    })

    const data = await res.json()
    if (data.code === 200) {
      showToast('注册成功！请登录')
      router.push('/login')
    } else {
      showToast(data.message || '注册失败')
    }
  } catch (e) {
    showToast('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
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
.register-btn {
  width: 100%;
  border-radius: 12px;
  color: #333;
  font-weight: 500;
}
.to-login {
  text-align: center;
  color: #ff9f00;
  margin-top: 15px;
  font-size: 14px;
}
</style>