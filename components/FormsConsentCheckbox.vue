<template>
    <label class="consent">
        <input type="checkbox" :checked="modelValue" @change="$emit('update:modelValue', $event.target.checked)" />
        我已閱讀並同意以上聲明
    </label>
</template>

<script setup>
defineProps({
    modelValue: {
        type: Boolean,
        required: true
    }
})
</script>

<style scoped>
.consent {
    display: block;
    margin: 1rem auto;
    font-weight: bold;
    text-align: center;
    width: fit-content;
}
</style>
