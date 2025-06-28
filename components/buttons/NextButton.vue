<template>
    <button class="next-button" :disabled="disabled" @click="emit('next')">
        {{ label }}
    </button>
</template>

<script setup>
const props = defineProps({
    label: { type: String, default: '下一步 →' },
    disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['next'])
</script>

<style>
.next-button {
    margin-bottom: 1rem;
    margin-left: 5rem;
    padding: 8px 14px;
    background-color: lightgray;
    border: 1px solid #999;
    border-radius: 6px;
    cursor: pointer;
}
</style>