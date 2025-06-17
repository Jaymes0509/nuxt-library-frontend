<template>
  <div class="reservation-page">
    <h1>📚 自習座位預約</h1>

    <SeatDatePicker v-model="selectedDate" />
    <SeatTimeSlot v-model="selectedSlot" />

    <SeatMap :date="selectedDate" :timeSlot="selectedSlot" @seat-selected="onSeatSelected" />

    <div v-if="selectedSeat" class="submit-section">
      <p>已選座位：{{ selectedSeat }}</p>
      <button @click="submitReservation">確認預約</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SeatDatePicker from '@/components/SeatDatePicker.vue'
import SeatTimeSlot from '@/components/SeatTimeSlot.vue'
import SeatMap from '@/components/SeatMap.vue'

const selectedDate = ref(null)
const selectedSlot = ref(null)
const selectedSeat = ref(null)

const onSeatSelected = (seatCode) => {
  selectedSeat.value = seatCode
}

const submitReservation = () => {
  if (!selectedDate.value || !selectedSlot.value || !selectedSeat.value) {
    alert('請先選擇日期、時段與座位')
    return
  }

  // TODO: 呼叫 API 提交預約
  alert(`預約成功：${selectedDate.value} ${selectedSlot.value} 座位 ${selectedSeat.value}`)
}
</script>

<style scoped>
.reservation-page {
  max-width: 800px;
  margin: auto;
  padding: 2rem;
}
.submit-section {
  margin-top: 20px;
}
</style>
