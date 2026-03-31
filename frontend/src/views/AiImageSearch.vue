<template>
  <div class="ai-search-page">
    <van-nav-bar
      title="AI 识图搜商品"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding: 16px;">
      <!-- 图片预览 -->
      <div class="preview-card" v-if="previewImage">
        <img :src="previewImage" class="preview-img" />
        <div class="preview-label">已选择</div>
        <van-button
          icon="cross"
          size="mini"
          type="danger"
          plain
          style="position: absolute; top:10px; right:10px"
          @click="clearImage"
        >清除</van-button>
      </div>

      <div class="action-card" v-else>
        <van-icon name="photo-o" size="40" color="#ff9f00" />
        <div style="margin-top:10px;">点击拍照，电脑/手机均可唤起摄像头</div>
      </div>

      <!-- 按钮 -->
      <div style="margin-top:20px; gap:10px; display:flex">
        <van-button icon="photograph" block @click="openCamera">拍照</van-button>
        <van-button icon="photo-o" block @click="openAlbum">相册</van-button>
      </div>

      <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />

      <!-- 摄像头弹窗 -->
      <van-popup v-model:show="cameraOpen" position="center" style="width:90%; max-width:400px">
        <div style="padding:15px; text-align:center;">
          <!-- 关键：加上 playsinline 防止视频被暂停 -->
          <video 
            ref="videoRef" 
            autoplay 
            playsinline 
            class="camera-video"
            style="width:100%; border-radius:8px; background:#000;"
          ></video>
          <div style="display:flex; gap:10px; margin-top:10px">
            <van-button color="#FFE181" block @click="takePhoto">拍照</van-button>
            <van-button block @click="closeCamera">取消</van-button>
          </div>
        </div>
      </van-popup>

      <van-button
        type="primary"
        color="#FFE181"
        block
        style="margin-top:15px; border-radius:12px"
        :loading="loading"
        :disabled="!previewImage"
        @click="startSearch"
      >开始AI识图搜索</van-button>

      <!-- 结果 -->
      <div v-if="keyword" class="result-card" style="margin-top:20px">
        <div class="title">AI 识别结果：{{keyword}}</div>
      </div>

      <div v-if="goodsList.length">
        <div class="title" style="margin:20px 0 10px">为你找到相似商品</div>
        <van-cell-group inset style="border-radius:12px">
          <van-cell
            v-for="g in goodsList"
            :key="g.id"
            :title="g.title"
            :value="`¥ ${g.price}`"
            is-link
            @click="goDetail(g.id)"
          />
        </van-cell-group>
      </div>

      <van-empty v-if="done && goodsList.length===0" style="margin-top:30px" description="未找到商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const previewImage = ref('')
const file = ref(null)
const fileInput = ref(null)
const videoRef = ref(null) // 视频 DOM 引用

const loading = ref(false)
const keyword = ref('')
const goodsList = ref([])
const done = ref(false)

// 摄像头
const cameraOpen = ref(false)
let stream = null

// 打开相册
const openAlbum = () => {
  fileInput.value.click()
}

// 打开摄像头（修复关键：加上 constraints 明确指定）
const openCamera = async () => {
  try {
    // 优先后置摄像头（手机），没有就用前置
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' },
      audio: false // 不需要麦克风
    })
    
    // 等待视频 DOM 加载完成
    await nextTick()
    
    const video = videoRef.value
    if (video) {
      video.srcObject = stream
      video.play().catch(err => {
        console.warn('视频自动播放被阻止，尝试手动播放:', err)
        // 某些浏览器需要用户交互才能播放，这里可以做额外处理
      })
    }
    
    cameraOpen.value = true
  } catch (e) {
    console.error('摄像头错误:', e)
    showToast('无法打开摄像头，请检查权限或使用相册')
    // 失败自动打开相册
    openAlbum()
  }
}

// 拍照（修复关键：加上明确的类型转换）
const takePhoto = () => {
  const video = videoRef.value
  if (!video || !stream) {
    showToast('摄像头未准备好')
    return
  }

  try {
    // 创建 Canvas 并绘制当前视频帧
    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth || 640 // 默认宽度
    canvas.height = video.videoHeight || 480 // 默认高度
    
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    // 转换为 Blob 文件对象
    canvas.toBlob(blob => {
      if (!blob) {
        showToast('拍照失败')
        return
      }
      // 转换为 File 对象，格式和相册上传一致
      file.value = new File([blob], 'camera_photo.jpg', { type: 'image/jpeg' })
      previewImage.value = URL.createObjectURL(blob)
      closeCamera()
    }, 'image/jpeg', 0.95) // 0.95 是质量
  } catch (err) {
    console.error('拍照逻辑失败:', err)
    showToast('拍照失败，请重试')
  }
}

// 关闭摄像头
const closeCamera = () => {
  cameraOpen.value = false
  if (stream) {
    stream.getTracks().forEach(track => {
      track.stop() // 停止所有轨道
    })
    stream = null
  }
}

// 选择相册图片
const onFileChange = (e) => {
  const f = e.target.files[0]
  if (!f) return
  file.value = f
  previewImage.value = URL.createObjectURL(f)
  e.target.value = '' // 重置
}

// 清除
const clearImage = () => {
  previewImage.value = ''
  file.value = null
  keyword.value = ''
  goodsList.value = []
  done.value = false
}

// ✅ 正确接口（完全匹配你的 Vite 代理）
const startSearch = async () => {
  if (!file.value) return showToast('请选择图片')
  
  loading.value = true
  keyword.value = ''
  goodsList.value = []
  done.value = false

  const fd = new FormData()
  fd.append('file', file.value)

  try {
    const res = await fetch('/api/ai/image/searchGoods', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + userStore.token },
      body: fd
    })

    const data = await res.json()
    if (data.code === 200) {
      keyword.value = data.data.keyword || '未识别'
      goodsList.value = data.data.goodsList || []
      showToast('识别成功')
    } else {
      showToast(data.msg || '识别失败，请更换图片')
    }
  } catch (err) {
    console.error('请求失败:', err)
    showToast('网络或后端服务异常')
  } finally {
    loading.value = false
    done.value = true
  }
}

const goDetail = (id) => router.push('/goods/detail/' + id)

// 组件销毁时强制关闭摄像头
onUnmounted(() => {
  closeCamera()
})
</script>

<style scoped>
.ai-search-page {
  background: #fff9e6;
  min-height: 100vh;
}
.moon-nav-bar {
  background: #FFE181;
}
.moon-nav-bar .van-nav-bar__title {
  color: #333;
}
.container {
  padding-bottom: 40px;
}
.action-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 20px;
  text-align: center;
  border: 1px dashed #ffc966
}
.preview-card {
  background: #fff;
  border-radius: 16px;
  padding: 15px;
  position: relative;
}
.preview-img {
  width: 100%;
  max-height: 300px;
  border-radius: 12px;
}
.preview-label {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ff9f00;
  color: #fff;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 6px;
}
.title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}
.result-card {
  background: #fff;
  padding: 15px;
  border-radius: 12px;
}
.camera-video {
  /* 确保视频比例正确 */
  aspect-ratio: 4/3;
  object-fit: cover;
}
</style>