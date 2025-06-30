<template>
    <div v-if="pending">載入中...</div>

    <div v-if="selectedSeat" class="selected-seat-msg">
        <span class="underline">📍您選的座位為: <strong>{{ selectedSeat }}</strong></span>
    </div>

    <div class="confirm-wrapper">
        <button class="confirm-button" @click="confirmSeat">
            確認
        </button>
    </div>

    <div class="seat-map-wrapper">
        <div class="seat-map">
            <!-- 大門圖 -->
            <div class="main-door">
                <div class="door-label">大門</div>
                <img src="/images/main-door.png" alt="大門" />
            </div>

            <!-- 門口圖示 -->
            <div v-for="(door, index) in doorPositions" :key="'door-' + index" class="door-icon"
                :style="{ top: door.top, left: door.left, right: door.right, bottom: door.bottom }">
                <img src="/images/door-icon.png" alt="門口" />
                <div class="door-label">門口</div>
            </div>

            <!-- 廁所 -->
            <div v-for="(toilet, index) in toiletPositions" :key="'toilet-' + index" class="toilet-icon"
                :style="{ top: toilet.top, left: toilet.left, right: toilet.right, bottom: toilet.bottom }">
                <img src="/images/toilet-icon.png" alt="廁所" />
            </div>

            <!-- 座位群組 -->
            <div v-for="(group, gIdx) in groupedSeats" :key="'group-' + gIdx" class="seat-group"
                :style="getGroupStyle(gIdx)">
                <div v-for="seatLabel in group" :key="seatLabel" class="seat-container"
                    :style="getContainerStyle(getSeatByLabel(seatLabel))" @click="handleClick(seatLabel)">
                    <div class="seat-wrapper">
                        <img :src="getSeatImage(seatLabel)"
                            :class="['seat-img', statusMap[seatLabel.toUpperCase()]?.toLowerCase()]" :alt="seatLabel" />
                    </div>
                    <div
                        :class="['seat-label', ['A', 'C', 'E'].includes(getSeatByLabel(seatLabel).label.charAt(0)) ? 'label-top' : 'label-bottom']">
                        {{ seatLabel }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

// 定義 props
const props = defineProps({
    selectedDate: String,
    selectedSlot: Object
})

const emit = defineEmits(['confirm'])

const selectedSeat = ref('')
const pending = ref(true)
const statusMap = ref({})

const confirmSeat = async () => {
    if (!selectedSeat.value) {
        alert('⚠️ 請先選擇一個座位再確認')
        return
    }
    alert('✅ 選擇座位為：' + selectedSeat.value)
    emit('confirm', selectedSeat.value)
    await refreshStatus()
}

const seats = ref(Array.from({ length: 6 }, (_, rowIdx) => {
    const rowChar = String.fromCharCode(65 + rowIdx)
    return Array.from({ length: 10 }, (_, colIdx) => ({
        label: `${rowChar}${colIdx + 1}`,
        row: rowIdx,
        col: colIdx
    }))
}).flat())

const groupedSeats = []
for (let row = 0; row < 6; row += 2) {
    for (let col = 0; col < 10; col += 2) {
        const r1 = String.fromCharCode(65 + row)
        const r2 = String.fromCharCode(65 + row + 1)
        groupedSeats.push([
            `${r1}${col + 1}`, `${r1}${col + 2}`,
            `${r2}${col + 1}`, `${r2}${col + 2}`
        ])
    }
}

const doorPositions = [
    { top: '-10px', right: '-20px' },
    { top: '-10px', left: '-930px' }
]

const toiletPositions = [
    { bottom: '250px', left: '0px' },
    { bottom: '250px', right: '0px' }
]

onMounted(refreshStatus)

async function refreshStatus() {
    pending.value = true

    try {
        // 基礎狀態 (AVAILABLE / BROKEN)
        const baseStatusRes = await fetch('http://localhost:8080/api/seats/status')
        const baseData = await baseStatusRes.json()

        // 1. 先標出預設狀態（AVAILABLE / BROKEN）
        const tempStatusMap = Object.fromEntries(baseData.map(s => [s.seatLabel, s.status]))

        // 2. 再向後端查指定時段已被預約的座位（動態加上 RESERVED）
        if (props.selectedDate && props.selectedSlot?.enum) {
            const occupiedRes = await fetch(
                `http://localhost:8080/api/seats/reservations/occupied?date=${props.selectedDate}&timeSlot=${props.selectedSlot.enum}`
            )
            const occupiedSeats = await occupiedRes.json()

            for (const label of occupiedSeats) {
                tempStatusMap[label] = 'RESERVED'
            }
        }

        // 3. 更新前端狀態
        statusMap.value = tempStatusMap

    } catch (err) {
        console.error('❌ 載入座位狀態失敗:', err)
    } finally {
        pending.value = false
    }
}

// 監聽 props 的變化,  加上 watch() 來即時更新狀態圖(immediate: true 代表元件一掛載就會先執行一次)
watch(
    () => [props.selectedDate, props.selectedSlot],
    () => {
        refreshStatus()
    },
    { immediate: true }
)


function getSeatByLabel(label) {
    return seats.value.find(seat => seat.label === label) || {}
}

function getGroupStyle(index) {
    return {
        position: 'absolute',
        top: `${Math.floor(index / 5) * 60 + 40}px`,
        left: `${(index % 5) * 50 + 30}px`
    }
}

function getSeatImage(label) {
    const status = statusMap.value[label.toUpperCase()]
    switch (status) {
        case 'AVAILABLE': return '/images/chair-available.png'
        case 'RESERVED': return '/images/chair-reserved.png'
        case 'BROKEN': return '/images/chair-broken.png'
        default: return '/images/chair-available.png'
    }
}

function getContainerStyle(seat) {
    return {
        position: 'absolute',
        top: `${seat.row * 90 + 40}px`,
        left: `${seat.col * 60 + 40}px`,
        width: '60px',
        height: '60px',
        textAlign: 'center'
    }
}

async function handleClick(label) {
    const seatKey = label.toUpperCase()
    const status = statusMap.value[seatKey]?.toUpperCase()

    if (!status) {
        alert('❌ 無法取得座位狀態')
        return
    }

    if (status === 'BROKEN') {
        alert('🛠️ 此座位維修中，無法預約')
        return
    }

    if (status === 'AVAILABLE') {
        try {
            const res = await fetch(
                `http://localhost:8080/api/seats/reservations/occupied?date=${props.selectedDate}&timeSlot=${props.selectedSlot.enum}`
            )
            const occupiedSeats = await res.json()
            if (occupiedSeats.includes(seatKey)) {
                alert('❌ 此座位已被其他人預約')
                return
            }
            selectedSeat.value = label
            alert('✅ 已選擇座位：' + label)
        } catch (err) {
            alert(`❌ 無法確認座位是否被預約：${err.message}`)
        }
        return
    }

    alert('❌ 無效的座位狀態')
}
</script>



<style scoped>
.selected-seat-msg {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    text-align: center;
    margin-top: -3rem;
}

.underline {
    display: block;
    background-image: url('/public/images/underline.png');
    /* 換成你的圖片路徑 */
    background-repeat: no-repeat;
    background-position: center;
    background-size: 15rem;
    /* 高度可調 */
    padding-bottom: 0.2em;
}

.confirm-wrapper {
    margin-top: 1.5rem;
    margin-bottom: -3rem;
    text-align: center;
    z-index: 999;
    position: relative;
}

.confirm-button {
    background-color: #fbbf24;
    color: #111;
    font-weight: bold;
    font-size: large;
    padding: 8px 16px;
    border-radius: 6px;
    border: none;
    cursor: pointer;
    display: inline-block;
    position: relative;
    transition: background-color 0.2s;
}

.confirm-button:hover {
    background-color: tomato;
}

.seat-map-wrapper {
    position: relative;
    padding: 20px;
}

.main-door {
    position: absolute;
    bottom: -20px;
    /* 可以根據需求放 top / left / right */
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
}

.main-door img {
    width: 15rem;
    height: 5.5rem;
}

.main-door .door-label {
    margin-top: -0.9rem;
    position: absolute;
    color: red;
    font-weight: bold;
}

.door-icon {
    position: absolute;
    top: 40px;
    /* 控制門口位置：你也可以改成 bottom/left/right */
    right: -1.3rem;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.door-icon img {
    width: 5rem;
    height: auto;
}

.door-label {
    font-size: 25px;
    color: #333;
    margin-top: 4px;
    margin-right: 0.5rem;
}


.toilet-icon {
    position: absolute;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.toilet-icon img {
    width: 60px;
    height: auto;
}

.seat-map {
    position: relative;
    width: 130vh;
    min-height: 105vh;
    padding-top: 2.5em;
    background: lightgray;
    border-radius: 10px;
    margin-top: 3rem;
    overflow: visible;
    /* margin: 0 auto 2rem; */
}

.seat-group {
    display: grid;
    grid-template-columns: repeat(2, 60px);
    grid-template-rows: repeat(2, 10px);
    gap: 5px;
    position: absolute;
}


.seat-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
    margin: 0 auto;
    /* margin: 0 auto 2rem; */
}


.seat-img.available {
    cursor: pointer;
}

.seat-img.reserved {
    cursor: url('/images/not_allowed_cursor.ico'), not-allowed;
}

.seat-img.broken {
    cursor: url('/images/repair_cursor.ico'), not-allowed;
}


.seat-label {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    font-size: 14px;
    font-weight: bold;
    white-space: nowrap;
}

.label-top {
    top: 0;
    transform: translateY(-100%);
}

.label-bottom {
    bottom: -1.2em;
    /* 顯示在椅子下方 */
}

.seat-container {
    position: absolute;
    width: 60px;
    height: 60px;
}

.seat-wrapper {
    width: 100%;
    height: 100%;
    transform-origin: center center;
}

html.accessible-mode .door-label {
    top: 0.6rem;
}

html.accessible-mode .confirm-button {
    color: white;
}
</style>