<template>
    <button type="submit">
        <slot />
    </button>
</template>

<script setup>
</script>

<style scoped>
button[type='submit'] {
    background-color: #007bff;
    color: white;
    padding: 10px;
    border: none;
    border-radius: 6px;
    font-size: 1rem;
    cursor: pointer;
}

button[type='submit']:hover {
    background-color: #0056b3;
}
</style>