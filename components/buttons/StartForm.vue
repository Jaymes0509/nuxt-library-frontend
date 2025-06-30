<template>
    <button :disabled="disabled" @click="$emit('next')" class="start-button">
        <slot />
    </button>
</template>

<script setup>
defineProps({
    disabled: {
        type: Boolean,
        default: false
    }
})
</script>

<style>
.start-button {
    display: block;
    margin: 0 auto 2rem;
    background-color: orange;
    color: black;
    padding: 12px 16px;
    border: 1px dashed #333;
    border-radius: 8px;
    font-size: 1rem;
    cursor: pointer;
}

.start-button:disabled {
    background-color: #ccc;
    color: #666;
    cursor: not-allowed;
}
</style>
