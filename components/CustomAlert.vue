<template>
    <transition name="custom-alert-fade">
        <div v-if="show" class="custom-alert-backdrop" @click="close">
            <div class="custom-alert-container" @click.stop>
                <div class="custom-alert-header">
                    <h3 class="custom-alert-title">{{ title }}</h3>
                    <button class="custom-alert-close-btn" @click="close">&times;</button>
                </div>
                <div class="custom-alert-body">
                    <p class="custom-alert-message">{{ message }}</p>
                </div>
                <div class="custom-alert-footer">
                    <button class="custom-alert-confirm-btn" @click="close">確認</button>
                </div>
            </div>
        </div>
    </transition>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

defineProps({
    show: {
        type: Boolean,
        required: true,
    },
    title: {
        type: String,
        default: '提示',
    },
    message: {
        type: String,
        required: true,
    },
})

const emit = defineEmits(['close'])

const close = () => {
    emit('close')
}
</script>

<style scoped>
/* Scoped CSS, no Tailwind here */
.custom-alert-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
}

.custom-alert-container {
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
    width: 90%;
    max-width: 400px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    overflow: hidden;
    transform: scale(1);
}

.custom-alert-header {
    padding: 16px 24px;
    border-bottom: 1px solid #e5e7eb;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.custom-alert-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: #111827;
    margin: 0;
}

.custom-alert-close-btn {
    background: none;
    border: none;
    font-size: 2rem;
    line-height: 1;
    color: #9ca3af;
    cursor: pointer;
    padding: 0;
    transition: color 0.2s ease;
}

.custom-alert-close-btn:hover {
    color: #111827;
}

.custom-alert-body {
    padding: 24px;
    line-height: 1.6;
}

.custom-alert-message {
    font-size: 1.5rem;
    color: #374151;
    margin: 0;
    white-space: pre-wrap;
    /* To respect newlines in the message */
}

.custom-alert-footer {
    padding: 16px 24px;
    background-color: #f9fafb;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid #e5e7eb;
}

.custom-alert-confirm-btn {
    background-color: #2563eb;
    color: white;
    border: none;
    border-radius: 8px;
    padding: 10px 20px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s ease, transform 0.1s ease;
}

.custom-alert-confirm-btn:hover {
    background-color: #1d4ed8;
}

.custom-alert-confirm-btn:active {
    transform: scale(0.98);
}

/* Transitions */
.custom-alert-fade-enter-active,
.custom-alert-fade-leave-active {
    transition: opacity 0.3s ease;
}

.custom-alert-fade-enter-from,
.custom-alert-fade-leave-to {
    opacity: 0;
}

.custom-alert-fade-enter-active .custom-alert-container {
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.custom-alert-fade-leave-active .custom-alert-container {
    transition: all 0.3s ease-in-out;
}

.custom-alert-fade-enter-from .custom-alert-container,
.custom-alert-fade-leave-to .custom-alert-container {
    transform: scale(0.95);
    opacity: 0;
}
</style>