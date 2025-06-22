<template>
    <button @click="delayedGoHome" class="go-home-button">回首頁</button>
</template>

<script setup>
import { useNavigation } from '@/composables/useNavigation'

const { goHome } = useNavigation()

const loading = ref(false)

const delayedGoHome = () => {
    loading.value = true
    setTimeout(() => {
        goHome()
    }, 3000)
}
</script>

<style scoped>
.go-home-button {
    background-color: #60a5fa;
    color: white;
    font-weight: bold;
    padding: 0.5rem 1.5rem;
    margin-top: 3rem;
    border-radius: 0.5rem;
    font-size: 1rem;
    border: none;
    cursor: pointer;
    transition: background-color 0.3s;
}

.go-home-button:hover {
    background-color: #3b82f6;
}
</style>