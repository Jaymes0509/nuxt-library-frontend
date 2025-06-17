<template>
    <div class="seat-time-slot">
        <label>🕒 請選擇時段：</label>
        <div class="options">
            <label v-for="slot in timeSlots" :key="slot.value" class="time-option">
                <input type="radio" name="timeSlot" :value="slot.value" v-model="modelValue"
                    @change="emit('nextStep')" />
                {{ slot.label }}
            </label>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
    modelValue: String
})
const emit = defineEmits(['update:modelValue', 'nextStep'])

const modelValue = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

const timeSlots = [
    {
        label: '09:00 - 11:00',
        value: { start: '09:00', end: '11:00' }
    },
    {
        label: '11:00 - 13:00',
        value: { start: '11:00', end: '13:00' }
    },
    {
        label: '13:00 - 15:00',
        value: { start: '13:00', end: '15:00' }
    },
    {
        label: '15:00 - 17:00',
        value: { start: '15:00', end: '17:00' }
    },
    {
        label: '17:00 - 19:00',
        value: { start: '17:00', end: '19:00' }
    },
    {
        label: '19:00 - 21:00',
        value: { start: '19:00', end: '21:00' }
    }
]
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
</style>
