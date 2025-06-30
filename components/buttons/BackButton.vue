<template>
    <button type="button" @click="goBack" class="back-button">
        ← 回上一頁
    </button>
</template>

<script setup>
const props = defineProps({
    step: {
        type: Number,
        required: true
    }
})

const emit = defineEmits(['update:step'])

const goBack = () => {
    if (props.step > 1) {
        emit('update:step', props.step - 1)
    }
}
</script>

<style>
.back-button {
    margin: 1rem;
    padding: 8px 14px;
    background-color: lightgray;
    border: 1px solid #999;
    border-radius: 6px;
    cursor: pointer;
    font-size: large
}
</style>
