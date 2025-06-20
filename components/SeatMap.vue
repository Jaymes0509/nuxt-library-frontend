<template>
    <div v-if="pending">載入中...</div>
    <div v-else class="seat-map">
        <div v-for="seat in seats" :key="seat.label"
            :class="['seat', statusMap[seat.label.toUpperCase()]?.toLowerCase() || 'available']" :style="getStyle(seat)"
            @click="handleClick(seat.label)">
            {{ seat.label }}
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const seats = ref([
    { label: 'A1', row: 0, col: 0 },
    { label: 'A2', row: 0, col: 1 },
    { label: 'B1', row: 1, col: 0 },
    { label: 'B2', row: 1, col: 1 }
])

const statusMap = ref({})

async function refreshStatus() {
    console.log('statusMap keys:', Object.keys(statusMap.value))

    const res = await fetch('http://localhost:8080/api/seats/status')
    const data = await res.json()
    statusMap.value = Object.fromEntries(data.map(s => [s.seatLabel, s.status]))
}

onMounted(refreshStatus)

async function handleClick(label) {
    const seatStatus = statusMap.value[label.toUpperCase()]
    console.log('當前狀態:', seatStatus)

    if (!seatStatus) {
        alert('❌ 無法取得座位狀態')
        return
    }

    const normalized = seatStatus.toLowerCase()

    if (normalized === 'broken') {
        alert('❌ 此座位已損壞，無法預約')
        return
    }

    if (normalized === 'reserved') {
        alert('❌ 這個座位已被預約')
        return
    }

    if (normalized === 'available') {
        const res = await fetch(`http://localhost:8080/api/seats/reserve/${label}`, {
            method: 'PUT'
        })
        const text = await res.text()
        alert(text)
        await refreshStatus()
        return
    }

    alert('❌ 無效的座位狀態')

}

function getStyle(seat) {
    return {
        top: `${seat.row * 60}px`,
        left: `${seat.col * 60}px`,
        position: 'absolute'
    }
}
</script>


<style scoped>
.seat-map {
    position: relative;
    width: 300px;
    height: 200px;
    background: #f3f3f3;
}

.seat {
    position: absolute;
    width: 50px;
    height: 50px;
    text-align: center;
    line-height: 50px;
    border: 1px solid black;
}

.available {
    background: #90ee90;
    cursor: pointer;
}

.reserved {
    background: #ccc;
    cursor: url('/images/not_allowed_cursor.ico'), not-allowed;
}

.broken {
    background: #ff6b6b;
    cursor: url('/images/repair_cursor.ico'), not-allowed;
}
</style>
