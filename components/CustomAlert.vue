<template>
    <transition name="custom-alert-fade">
        <div v-if="show" class="custom-alert-backdrop" @click.self="handleClose">
            <div class="custom-alert-container" @click.stop>
                <div class="custom-alert-header">
                    <h3 class="custom-alert-title">{{ title }}</h3>
                    <button class="custom-alert-close-btn" @click="handleClose">&times;</button>
                </div>
                <div class="custom-alert-body">
                    <p class="custom-alert-message">{{ message }}</p>
                    <ul v-if="items && items.length" class="custom-alert-items-list">
                        <li v-for="(item, index) in items" :key="index" class="custom-alert-item">
                            {{ item }}
                        </li>
                    </ul>
                </div>
                <div class="custom-alert-footer">
                    <button class="custom-alert-btn custom-alert-confirm-btn" @click="handleConfirm">{{ confirmText
                        }}</button>
                    <button v-if="type === 'confirm'" class="custom-alert-btn custom-alert-cancel-btn"
                        @click="handleClose">{{ cancelText }}</button>
                </div>
            </div>
        </div>
    </transition>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

defineProps({
    show: { type: Boolean, required: true },
    title: { type: String, default: '提示' },
    message: { type: String, required: true },
    items: { type: Array, default: () => [] },
    type: { type: String, default: 'alert' }, // 'alert' or 'confirm'
    confirmText: { type: String, default: '確認' },
    cancelText: { type: String, default: '取消' },
})

const emit = defineEmits(['close', 'confirm'])

const handleClose = () => {
    emit('close')
}

const handleConfirm = () => {
    emit('confirm')
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
    max-height: 60vh;
    overflow-y: auto;
}

.custom-alert-message {
    font-size: 1rem;
    color: #374151;
    margin: 0;
    white-space: pre-wrap;
    /* To respect newlines in the message */
    margin-bottom: 16px;
}

.custom-alert-items-list {
    list-style-type: none;
    padding-left: 0;
    margin-top: 12px;
    margin-bottom: 0;
    text-align: left;
    background-color: #f9fafb;
    border-radius: 8px;
    padding: 12px;
}

.custom-alert-item {
    font-size: 0.95rem;
    color: #4b5563;
    padding: 8px 12px;
    border-bottom: 1px solid #f3f4f6;
}

.custom-alert-item:last-child {
    border-bottom: none;
}

.custom-alert-footer {
    padding: 16px 24px;
    background-color: #f9fafb;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    border-top: 1px solid #e5e7eb;
}

.custom-alert-btn {
    border: none;
    border-radius: 8px;
    padding: 10px 20px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.custom-alert-confirm-btn {
    background-color: #2563eb;
    color: white;
}

.custom-alert-confirm-btn:hover {
    background-color: #1d4ed8;
}

.custom-alert-cancel-btn {
    background-color: #ffffff;
    color: #4b5563;
    border: 1px solid #d1d5db;
}

.custom-alert-cancel-btn:hover {
    background-color: #f3f4f6;
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