<template>
    <div class="seat-time-slot">
        <label>🕒 請選擇預計就座時段：</label>
        <div class="options">
            <label v-for="slot in timeSlots" :key="slot.label" class="time-option"
                :class="{ disabled: isSlotDisabled(slot) }">
                <input type="radio" name="timeSlot" :value="slot.value" v-model="modelValue" />
                {{ slot.label }}
            </label>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
    modelValue: Object, // slot value 是物件 { start: '09:00', end: '11:00' }
    selectedDate: String
})

const emit = defineEmits(['update:modelValue'])

const modelValue = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

const now = new Date()
const todayStr = now.toISOString().split('T')[0]

const timeSlots = [
    {
        label: '09:00 - 11:00',
        value: { start: '09:00', end: '11:00', enum: 'SLOT_09_11' }
    },
    {
        label: '11:00 - 13:00',
        value: { start: '11:00', end: '13:00', enum: 'SLOT_11_13' }
    },
    {
        label: '13:00 - 15:00',
        value: { start: '13:00', end: '15:00', enum: 'SLOT_13_15' }
    },
    {
        label: '15:00 - 17:00',
        value: { start: '15:00', end: '17:00', enum: 'SLOT_15_17' }
    },
    {
        label: '17:00 - 19:00',
        value: { start: '17:00', end: '19:00', enum: 'SLOT_17_19' }
    },
    {
        label: '19:00 - 21:00',
        value: { start: '19:00', end: '21:00', enum: 'SLOT_19_21' }
    }
]

// 判斷是否過時（只針對今天）
const isSlotDisabled = (slot) => {
    if (props.selectedDate !== todayStr) return false
    const nowMinutes = now.getHours() * 60 + now.getMinutes()

    const [endH, endM] = slot.value.end.split(':').map(Number)

    const slotEndMinutes = endH * 60 + endM
    return nowMinutes >= slotEndMinutes  // 過了結束時間才禁用
}
</script>

<style scoped>
.seat-time-slot {
    margin-bottom: 1rem;
}

.options {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-top: 0.5rem;
}

.time-option {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 16px;
}

.time-option.disabled {
    opacity: 0.5;
    pointer-events: none;
}
</style>
