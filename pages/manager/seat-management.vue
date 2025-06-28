<template>
    <div>
        <h1>座位管理</h1>
        <div class="seat-list">
            <div v-for="seat in seats" :key="seat.seatLabel" class="seat-item">
                <img :src="getSeatImage(seat.status)" alt="seat icon" class="seat-icon"
                    :title="seat.hasReservation ? '已有預約，無法恢復為可用' : ''" />
                <span class="label">{{ seat.seatLabel }}</span>
                <span :class="seat.status.toLowerCase()">{{ seat.status }}</span>

                <!-- 標記損壞 -->
                <button v-if="seat.status !== 'BROKEN'" @click="markAsBroken(seat.seatLabel)">
                    標記損壞
                </button>

                <!-- 恢復可用（禁用狀態處理） -->
                <button v-if="seat.status === 'BROKEN'" @click="markAsAvailable(seat.seatLabel)"
                    :disabled="seat.hasReservation" :title="seat.hasReservation ? '此座位已有預約，無法恢復' : ''">
                    恢復可用
                    <span v-if="seat.hasReservation">🔒</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { eventBus } from '@/utils/event-bus'

const seats = ref([])

onMounted(() => {
    loadSeats()

    // 監聽全域事件「reservation-cancelled」
    eventBus.on('reservation-cancelled', handleReservationCancelled)
})

onUnmounted(() => {
    // 離開頁面時解除監聽，避免記憶體洩漏
    eventBus.off('reservation-cancelled', handleReservationCancelled)
})

async function handleReservationCancelled() {
    console.log('📢 收到 reservation-cancelled 事件 → 重新加載座位')
    await loadSeats()
}

async function loadSeats() {
    // 查詢所有座位
    const res = await fetch('http://localhost:8080/api/seats/status')
    const allSeats = await res.json()
    console.log('🪑 所有座位:', allSeats)

    // 查詢有未來預約的座位 seatLabel 陣列
    const reservedRes = await fetch('http://localhost:8080/api/seats/reservations/upcoming')
    const reservedLabels = await reservedRes.json()
    console.log('📌 有預約的座位:', reservedLabels)

    // 合併資料，加上 hasReservation 屬性
    seats.value = allSeats.map(seat => ({
        ...seat,
        hasReservation: reservedLabels.includes(seat.seatLabel)
    }))
}

function getSeatImage(status) {
    switch (status) {
        case 'AVAILABLE':
            return '/images/chair-available.png'
        case 'BROKEN':
            return '/images/chair-broken.png'
        default:
            return '/images/chair-available.png'
    }
}

async function markAsBroken(label) {
    try {
        const res = await fetch(`http://localhost:8080/api/seats/mark-broken/${label}`, {
            method: 'PUT'
        })
        if (res.ok) {
            alert('✅ 已標記為損壞')
            await loadSeats()
        } else {
            alert('❌ 標記失敗')
        }
    } catch (err) {
        alert('❌ 發生錯誤：' + err.message)
    }
}

async function markAsAvailable(label) {
    try {
        const res = await fetch(`http://localhost:8080/api/seats/mark-available/${label}`, {
            method: 'PUT'
        })
        if (res.ok) {
            alert('✅ 已恢復為可用')
            await loadSeats()
        } else {
            const msg = await res.text()
            alert('❌ 恢復失敗：' + msg)
        }
    } catch (err) {
        alert('❌ 發生錯誤：' + err.message)
    }
}
</script>

<style scoped>
.seat-list {
    margin-top: 20px;
}

.seat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 12px 0;
}

.seat-icon {
    width: 40px;
    height: 40px;
}

.label {
    font-weight: bold;
    width: 60px;
}

.available {
    color: green;
}

.broken {
    color: red;
}
</style>
