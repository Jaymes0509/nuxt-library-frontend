<template>
    <button type="button" @click="$emit('reset')" class="reset-button">
        🔁 重新填寫
    </button>
</template>

<script setup>
</script>

<style scoped>
.reset-button {
    margin: 1rem;
    padding: 8px 14px;
    background-color: lightgray;
    border: 1px solid #999;
    border-radius: 6px;
    cursor: pointer;
}
</style>