<template>
    <div class="seat-date-picker">
        <label for="date">📅 請選擇日期：</label>
        <input id="date" type="date" :min="today" :max="maxDate" v-model="modelValue" @change="handleChange" required />
    </div>
</template>

<script setup>
const props = defineProps({
    modelValue: String,            // 雙向綁定值
})

const emit = defineEmits([
    'update:modelValue',
    'nextStep' // 自訂事件，通知外層跳下一步
])

// 今天日期
const today = new Date().toISOString().split('T')[0]

// 七天後日期
const maxDate = new Date()
maxDate.setDate(maxDate.getDate() + 7)
const maxDateStr = maxDate.toISOString().split('T')[0]

// 雙向綁定處理
const modelValue = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

// 當日期改變時，通知外層跳下一步
const handleChange = () => {
    if (modelValue.value) {
        emit('nextStep') // ⬅️ 這就是通知外層改 step
    }
}
</script>

<style scoped>
.seat-date-picker {
    margin-bottom: 1rem;
}

label {
    font-weight: bold;
    margin-right: 0.5rem;
}

input[type="date"] {
    padding: 6px;
    font-size: 16px;
    border: 1px solid #ccc;
    border-radius: 4px;
}
</style>
