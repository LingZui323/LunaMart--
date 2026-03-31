<template>
  <div class="merchant-goods-page">
    <van-nav-bar
      title="商品管理"
      fixed
      left-arrow
      @click-left="$router.back()"
      class="moon-nav-bar"
    />

    <div class="container" style="margin-top: 46px; padding-bottom: 20px">
      <div class="filter-bar">
        <van-dropdown-menu active-color="#ff9f00">
          <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="handleFilter" />
        </van-dropdown-menu>
        <van-button size="small" type="primary" color="#ff9f00" class="add-btn" @click="handleAddGoods">
          + 新增商品
        </van-button>
      </div>

      <div v-for="item in filteredGoods" :key="item.id" class="goods-card">
        <div class="goods-img">
          <!-- 🔥 已修复：封面图优先 + 保底逻辑 -->
          <van-image :src="item.coverImage || item.imageList?.[0]?.imageUrl || 'https://picsum.photos/80'" width="80" height="80" fit="cover" radius="8" />
        </div>

        <div class="goods-info">
          <div class="goods-title">{{ item.title }}</div>
          <div class="goods-status" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </div>
          <div class="goods-desc">{{ item.description || '暂无描述' }}</div>
          <div class="goods-price">¥{{ item.price }}</div>
          <div class="goods-stock">库存：{{ item.stock }}件</div>
        </div>

        <div class="goods-stats">
          <div class="stat-item">
            <van-icon name="like-o" size="14" />
            <span>{{ item.likeCount }}</span>
          </div>
          <div class="stat-item">
            <van-icon name="star-o" size="14" />
            <span>{{ item.collectCount }}</span>
          </div>
          <div class="stat-item">
            <van-icon name="comment-o" size="14" />
            <span>{{ item.commentCount }}</span>
          </div>
          <div class="stat-item">
            <van-icon name="eye-o" size="14" />
            <span>{{ item.viewCount }}</span>
          </div>
        </div>

        <div class="goods-actions">
          <van-switch
            :model-value="item.status === 'ON_SALE'"
            size="20"
            :disabled="!canToggleStatus(item.status)"
            @change="(val) => handleToggleStatus(item, val)"
          />
          <van-button size="mini" type="primary" plain @click="handleEditGoods(item)">编辑</van-button>
        </div>
      </div>

      <div v-if="filteredGoods.length === 0" class="empty-state">
        <van-empty description="暂无商品" />
      </div>
    </div>

    <van-popup v-model:show="showEditDialog" position="bottom" :style="{ height: '70%', padding: '20px', borderRadius: '12px 12px 0 0' }">
      <div class="dialog-header">
        <span @click="showEditDialog = false">取消</span>
        <span class="title">{{ isEdit ? '编辑商品' : '新增商品' }}</span>
        <span @click="handleSaveGoods" class="confirm">保存</span>
      </div>
      <div class="edit-form">
        <van-field v-model="editForm.title" label="商品标题" placeholder="请输入商品标题" />
        <van-field label="商品分类" readonly is-link @click="showCategoryPicker = true">
          <template #input>
            {{ editForm.categoryId ? categoryList.find(c => c.id === editForm.categoryId)?.name : '请选择分类' }}
          </template>
        </van-field>
        <van-field v-model="editForm.price" label="价格" type="number" placeholder="请输入价格" />
        <van-field v-model="editForm.stock" label="库存" type="number" placeholder="请输入库存" />
        <van-field v-model="editForm.description" label="描述" type="textarea" rows="3" placeholder="请输入描述" />
        
        <van-field label="商品图片">
          <template #input>
            <van-uploader
              v-model="tempImages"
              :max-count="3"
              accept="image/*"
              @delete="handleDeleteImage"
            />
          </template>
        </van-field>
      </div>

      <van-popup v-model:show="showCategoryPicker" position="bottom" :style="{ height: '50%' }">
        <van-tree-select
          :items="treeItems"
          :main-active-index="mainActiveIndex"
          :active-id="activeSubId"
          @click-nav="onMainCategoryChange"
          @click-item="onSubCategoryConfirm"
        />
      </van-popup>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

const filterStatus = ref('')
const statusOptions = ref([
  { text: '全部', value: '' },
  { text: '售卖中', value: 'ON_SALE' },
  { text: '草稿', value: 'DRAFT' },
  { text: '审核中', value: 'PENDING_AUDIT' },
  { text: '已下架', value: 'OUT_OF_STOCK' },
  { text: '已驳回', value: 'REJECTED' },
])

const goods = ref([])
const showEditDialog = ref(false)
const isEdit = ref(false)

const editForm = ref({
  id: null,
  title: '',
  price: 0,
  stock: 0,
  description: '',
  categoryId: null,
})

// ✅ 临时图片（只预览，不上传）
const tempImages = ref([])

const categoryList = ref([])
const mainCategories = ref([])
const subCategories = ref([])
const showCategoryPicker = ref(false)
const mainActiveIndex = ref(0)
const activeSubId = ref(null)

// ============================
// 加载分类
// ============================
const loadCategories = async () => {
  try {
    const res = await fetch('/api/category/list')
    const data = await res.json()
    if (data.code === 200) {
      categoryList.value = data.data || []
      mainCategories.value = categoryList.value.filter(c => c.parentId === 0 || c.level === 1)
      if (mainCategories.value.length > 0) {
        const firstId = mainCategories.value[0].id
        subCategories.value = categoryList.value.filter(c => c.parentId === firstId)
      }
    }
  } catch (e) {
    showToast('加载分类失败')
  }
}

// ============================
// 加载商品列表 + 封面图
// ============================
const loadGoods = async () => {
  try {
    const res = await fetch('/api/goods/my', {
      headers: { 'Authorization': 'Bearer ' + userStore.token }
    })
    const data = await res.json()
    if (data.code === 200) {
      const list = data.data || []
      await Promise.all(list.map(async (item) => {
        try {
          const r = await fetch(`/api/goods/image/list/${item.id}`)
          const d = await r.json()
          if (d.code === 200) {
            const cover = d.data.find(i => i.isCover === 1)
            item.coverImage = cover ? cover.imageUrl : ''
          }
        } catch (_) {}
      }))
      goods.value = list
    }
  } catch (e) {
    showToast('加载失败')
  }
}

// ============================
// 筛选
// ============================
const filteredGoods = computed(() => {
  if (!filterStatus.value) return goods.value
  return goods.value.filter(g => g.status === filterStatus.value)
})

const treeItems = computed(() => {
  return mainCategories.value.map(m => ({
    text: m.name,
    id: m.id,
    children: categoryList.value.filter(c => c.parentId === m.id).map(s => ({ text: s.name, id: s.id }))
  }))
})

const onMainCategoryChange = (index) => {
  mainActiveIndex.value = index
  const id = mainCategories.value[index].id
  subCategories.value = categoryList.value.filter(c => c.parentId === id)
  activeSubId.value = null
}

const onSubCategoryConfirm = (item) => {
  editForm.value.categoryId = item.id
  showCategoryPicker.value = false
}

// ============================
// 新增
// ============================
const handleAddGoods = () => {
  isEdit.value = false
  editForm.value = { id: null, title: '', price: 0, stock: 0, description: '', categoryId: null }
  tempImages.value = []
  showEditDialog.value = true
}

// ============================
// 编辑
// ============================
const handleEditGoods = async (item) => {
  isEdit.value = true
  editForm.value = { ...item }
  editForm.value.id = item.id

  // 加载已有图片
  try {
    const r = await fetch(`/api/goods/image/list/${item.id}`)
    const d = await r.json()
    if (d.code === 200) {
      tempImages.value = d.data.map(img => ({
        id: img.id,
        url: img.imageUrl
      }))
    }
  } catch (_) {}

  if (item.categoryId) {
    const sub = categoryList.value.find(c => c.id === item.categoryId)
    if (sub) {
      const idx = mainCategories.value.findIndex(m => m.id === sub.parentId)
      if (idx !== -1) mainActiveIndex.value = idx
    }
  }

  showEditDialog.value = true
}

// ============================
// 🔥 终极版：直接从 tempImages 里删除 params 这个元素
// ============================
const handleDeleteImage = async (params) => {
  console.log("=== 开始删除图片 ===")
  console.log("待删除的图片元素:", params)

  // 1. 先处理后端删除（如果图片有 id）
  if (params.id) {
    try {
      console.log("✅ 图片有 id，调用后端删除:", params.id)
      const res = await fetch(`/api/goods/image/${params.id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': 'Bearer ' + userStore.token,
          'Content-Type': 'application/json'
        }
      })
      const data = await res.json()
      if (data.code !== 200) {
        showToast("删除失败：" + (data.msg || "服务器错误"))
        return
      }
      console.log("✅ 后端删除成功！")
    } catch (err) {
      console.error("删除异常:", err)
      showToast("删除请求失败")
      return
    }
  }

  // 2. 直接从 tempImages 里删除这个元素（v-model 自动同步）
  const idx = tempImages.value.indexOf(params)
  if (idx !== -1) {
    tempImages.value.splice(idx, 1)
    showToast("删除成功")
  } else {
    // 兜底：用 id/url 匹配删除
    const matchIdx = tempImages.value.findIndex(img => 
      (img.id && img.id === params.id) || (img.url && img.url === params.url)
    )
    if (matchIdx !== -1) {
      tempImages.value.splice(matchIdx, 1)
      showToast("删除成功")
    } else {
      showToast("删除失败：找不到该图片")
    }
  }
}

// ============================
// ✅ 保存：统一上传图片（核心修复）
// ============================
const handleSaveGoods = async () => {
  if (!editForm.value.title) return showToast('请输入标题')
  if (editForm.value.price <= 0) return showToast('价格必须大于0')
  if (!editForm.value.categoryId) return showToast('请选择分类')

  try {
    // 1. 保存商品
    const url = isEdit.value ? '/api/goods/update' : '/api/goods/save'
    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + userStore.token
      },
      body: JSON.stringify({
        ...editForm.value,
        price: String(editForm.value.price)
      })
    })
    const data = await res.json()
    if (data.code !== 200) return showToast('保存失败')

    const goodsId = data.data.id || editForm.value.id

    // 2. 上传新图片
    for (const img of tempImages.value) {
      if (img.id) continue
      if (!img.file) continue
      const fd = new FormData()
      fd.append('file', img.file)
      const upRes = await fetch(`/api/goods/image/upload?goodsId=${goodsId}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + userStore.token },
        body: fd
      })
      const upData = await upRes.json()
      if (upData.code !== 200) showToast('图片上传失败')
    }

    // ✅ 新增：自动把第一张图片设为封面
    if (tempImages.value.length > 0) {
      const firstImg = tempImages.value[0]
      if (firstImg.id) {
        await fetch(`/api/goods/image/set-cover/${firstImg.id}`, {
          method: 'PUT',
          headers: {
            'Authorization': 'Bearer ' + userStore.token,
            'Content-Type': 'application/json'
          }
        })
      }
    }

    showToast(isEdit.value ? '修改成功' : '创建成功')
    showEditDialog.value = false
    setTimeout(loadGoods, 300)
  } catch (e) {
    showToast('保存失败')
  }
}

// ============================
// 上下架（已兼容：上架 + 下架）
// ============================
const handleToggleStatus = async (item, val) => {
  try {
    await showConfirmDialog(`确认${val ? '上架' : '下架'}？`)
    
    if (val) {
      // ✅ 上架：调用新接口 /online
      await fetch(`/api/goods/online/${item.id}`, {
        method: 'POST', 
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      showToast('上架成功')
    } else {
      // ❌ 下架：原有逻辑
      await fetch(`/api/goods/offline/${item.id}`, {
        method: 'POST', 
        headers: { Authorization: 'Bearer ' + userStore.token }
      })
      showToast('已下架')
    }
    
    loadGoods()
  } catch (_) {
    showToast('已取消')
  }
}

const canToggleStatus = (s) => ['DRAFT', 'OUT_OF_STOCK', 'REJECTED', 'ON_SALE'].includes(s)

const getStatusText = (s) => {
  const m = { ON_SALE:'售卖中', DRAFT:'草稿', PENDING_AUDIT:'审核中', OUT_OF_STOCK:'已下架', REJECTED:'已驳回', PENDING_AI_AUDIT:'AI审核中' }
  return m[s] || s
}
const getStatusClass = (s) => {
  const m = { ON_SALE:'status-on-sale', DRAFT:'status-draft', PENDING_AUDIT:'status-pending', OUT_OF_STOCK:'status-off-sale', REJECTED:'status-rejected', PENDING_AI_AUDIT:'status-pending-ai' }
  return m[s] || 'status-default'
}

onMounted(async () => {
  await loadCategories()
  await loadGoods()
})

const handleFilter = () => {}
</script>

<style scoped>
.merchant-goods-page { background: #fff9e6; min-height: 100vh }
.moon-nav-bar { background: #FFE181 }
.container { padding: 10px }
.filter-bar { display: flex; align-items: center; justify-content: space-between; background: #fff; border-radius: 12px; padding: 10px; margin-bottom: 15px }
.add-btn { background: #ff9f00; color: #fff; border: none }
.goods-card { display: flex; gap: 12px; background: #fff; border-radius: 12px; padding: 12px; margin-bottom: 10px }
.goods-img { flex-shrink: 0 }
.goods-info { flex: 1 }
.goods-title { font-size: 15px; font-weight: bold; color: #333; margin-bottom: 4px }
.goods-desc { font-size: 12px; color: #666; margin-bottom: 6px }
.goods-price { font-size: 16px; color: #ff4444; font-weight: bold }
.goods-stock { font-size: 12px; color: #999 }
.goods-stats { display: flex; flex-direction: column; gap: 8px; padding: 0 8px }
.stat-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #666 }
.goods-actions { display: flex; flex-direction: column; gap: 8px }
.dialog-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px }
.dialog-header .confirm { color: #ff9f00 }
.edit-form { display: flex; flex-direction: column; gap: 12px }
.empty-state { padding: 60px 0; text-align: center }
.goods-status { display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 11px; margin-bottom: 6px }
.status-on-sale { background: #e7f7ed; color: #07c160 }
.status-draft { background: #f5f5f5; color: #969799 }
.status-pending { background: #fef3e0; color: #ff976a }
.status-pending-ai { background: #e8f3ff; color: #1989fa }
.status-off-sale { background: #f0f0f0; color: #646565 }
.status-rejected { background: #fde2e2; color: #ee0a24 }
</style>