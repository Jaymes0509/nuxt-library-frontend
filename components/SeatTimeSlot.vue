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
    { label: '上午（09:00–12:00）', value: 'morning' },
    { label: '下午（13:00–17:00）', value: 'afternoon' },
    { label: '晚上（18:00–22:00）', value: 'evening' }
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
