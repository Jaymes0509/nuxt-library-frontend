<template>
    <div v-if="pending">載入中...</div>

    <div v-else class="seat-map">
        <div v-for="seat in seats.value" :key="seat.id" :class="['seat', seat.status.toLowerCase()]"
            :style="getStyle(seat)">
            {{ seat.label }}
        </div>
    </div>
</template>

<script setup>
import { useFetch } from '#app'

const { data: seats, pending, error } = useFetch('/api/seats/all')

const getStyle = (seat) => ({
    top: `${seat.rowIndex * 60}px`,
    left: `${seat.colIndex * 60}px`,
    position: 'absolute'
})

watchEffect(() => {
    console.log('seats：', seats.value)
})

</script>

<style scoped>
.seat-map {
    position: relative;
    width: 100%;
    height: 100%;
    min-width: 400px;
    min-height: 360px;
    background-color: #f2f2f2;
    border-radius: 10px;
    overflow: auto;
}

.seat {
    position: absolute;
    width: 50px;
    height: 50px;
    border: 1px solid #000;
    text-align: center;
    line-height: 50px;
    background-color: lightgray;
}

.available {
    background-color: #90ee90;
    color: black;
}

.reserved {
    background-color: #ddd;
    color: #666;
}

.broken {
    background-color: #ff6b6b;
    color: white;
}
</style>
