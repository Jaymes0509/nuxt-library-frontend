<template>
    <div class="reservation-summary">
        <h2>✅ 預約完成</h2>
        <p>📅 預約日期：<strong>{{ selectedDate }}</strong></p>
        <p>⏰ 預計就座時段：<strong>{{ selectedSlot }}</strong></p>
        <p>💺 選擇座位：<strong>{{ selectedSeat }}</strong></p>
    </div>
    <ButtonsGoHome />
</template>

<script setup>
defineProps({
    selectedDate: { type: String, required: true },
    selectedSlot: { type: String, required: true },
    selectedSeat: { type: String, required: true }
})
</script>

<style scoped>
.reservation-summary {
    margin-top: 2rem;
    text-align: center;
    font-size: 18px;
    background-color: #fffbe6;
    border: 2px solid #facc15;
    border-radius: 10px;
    padding: 2rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.reservation-summary p {
    margin: 1rem 0;
}
</style>