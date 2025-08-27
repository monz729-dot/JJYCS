<template>
  <div>
    <h1>주문 접수</h1>
    <form>
      <div>
        <label>배송 유형</label>
        <input type="radio" name="shippingType" value="sea" v-model="shippingType">
        <label>해상</label>
        <input type="radio" name="shippingType" value="air" v-model="shippingType">
        <label>항공</label>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const shippingType = ref('sea')
</script>

<style scoped>
/* 스타일 */
</style>