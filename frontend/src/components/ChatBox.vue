<template>
  <div>
    <van-field
      v-model="msg"
      placeholder="输入消息..."
      button
      @click-button="send"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
const msg = ref('')
const emit = defineEmits(['send'])
const send = () => {
  emit('send', msg.value)
  msg.value = ''
}
</script>