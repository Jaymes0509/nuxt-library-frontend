<template>
    <div class="seat-map">
        <div v-for="seat in seats" :key="seat.id" class="seat" :class="seat.status" :style="getPositionStyle(seat)"
            @click="handleClick(seat)">
            {{ seat.label }}
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const seats = ref([])

// 模擬 API 載入（未來請改成 useFetch('/api/seats')）
const fetchSeats = async () => {
    seats.value = [
        { id: 1, label: 'A01', status: 'available', row: 0, col: 0 },
        { id: 2, label: 'A02', status: 'available', row: 0, col: 1 },
        { id: 3, label: 'A03', status: 'reserved', row: 0, col: 2 },
        { id: 4, label: 'A04', status: 'available', row: 0, col: 3 },
        { id: 5, label: 'A05', status: 'available', row: 1, col: 1 },
        { id: 6, label: 'A06', status: 'reserved', row: 1, col: 2 },
        { id: 7, label: 'A07', status: 'available', row: 2, col: 0 },
        { id: 8, label: 'A08', status: 'available', row: 2, col: 1 },
        { id: 9, label: 'A09', status: 'reserved', row: 2, col: 2 },
        { id: 10, label: 'A10', status: 'available', row: 2, col: 3 },
    ]
}

const getPositionStyle = (seat) => {
    const size = 48
    return {
        top: `${seat.row * (size + 12)}px`,
        left: `${seat.col * (size + 12)}px`
    }
}

const handleClick = (seat) => {
    if (seat.status !== 'available') return
    seats.value = seats.value.map(s => ({
        ...s,
        status:
            s.id === seat.id
                ? 'selected'
                : s.status === 'selected'
                    ? 'available'
                    : s.status
    }))
}

onMounted(fetchSeats)
</script>

<style scoped>
.seat-map {
    position: relative;
    width: 300px;
    height: 250px;
    background-color: #f9f9f9;
    border: 1px solid #ccc;
    border-radius: 12px;
}

/* 共用樣式 */
.seat {
    position: absolute;
    width: 48px;
    height: 48px;
    border-radius: 8px;
    text-align: center;
    line-height: 48px;
    font-weight: bold;
    font-size: 0.85rem;
    user-select: none;
    transition: 0.2s;
    cursor: pointer;
}

/* 狀態樣式 */
.available {
    background: #c8facc;
    color: #000;
    border: 2px solid #28a745;
}

.reserved {
    background: #eee;
    color: #999;
    border: 2px solid #ccc;
    cursor: not-allowed;
}

.selected {
    background: #ffa726;
    color: white;
    border: 2px solid #fb8c00;
}
</style>
