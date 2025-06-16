<template>
    <div class="seat-map">
        <div class="legend">
            <span class="seat available"></span> 可預約
            <span class="seat unavailable"></span> 已預約
            <span class="seat selected"></span> 已選擇
        </div>

        <div class="grid">
            <div v-for="seat in seatList" :key="seat" :class="[
                'seat',
                selectedSeat === seat ? 'selected' :
                    isAvailable(seat) ? 'available' : 'unavailable'
            ]" @click="selectSeat(seat)">
                {{ seat }}
            </div>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
    seatList: {
        type: Array,
        default: () => ['A1', 'A2', 'A3', 'A4', 'A5', 'A6']
    },
    availableSeats: {
        type: Array,
        default: () => [] // e.g., ['A1', 'A2', 'A4']
    }
})

const emit = defineEmits(['seat-selected'])

const selectedSeat = ref(null)

const isAvailable = (seat) => props.availableSeats.includes(seat)

const selectSeat = (seat) => {
    if (!isAvailable(seat)) return
    selectedSeat.value = seat
    emit('seat-selected', seat)
}
</script>

<style scoped>
.seat-map {
    padding: 1rem;
    max-width: 400px;
    margin: auto;
    text-align: center;
}

.grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 10px;
    margin-top: 1rem;
}

.seat {
    padding: 0.5rem;
    border-radius: 6px;
    font-weight: bold;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;
}

.available {
    background-color: #d0f0d0;
    border-color: #60c060;
}

.unavailable {
    background-color: #eee;
    color: #999;
    cursor: not-allowed;
}

.selected {
    background-color: #ffa726;
    color: white;
    border-color: #f57c00;
}

.legend {
    margin-bottom: 0.5rem;
    font-size: 14px;
    display: flex;
    justify-content: center;
    gap: 1rem;
}

.legend .seat {
    width: 20px;
    height: 20px;
    display: inline-block;
}
</style>
