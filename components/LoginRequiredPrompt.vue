<template>
    <div class="login-required">
        <div class="login-required-icon">🔒</div>
        <h2>需要登入會員</h2>
        <p>{{ message }}</p>
        <button @click="goToLogin" class="login-required-btn">前往登入</button>
    </div>
</template>

<script setup>
const props = defineProps({
    message: {
        type: String,
        default: '您需要登入會員才能使用相關功能'
    }
})

const router = useRouter()

const goToLogin = () => {
    router.push('/login')
}
</script>

<style>
.login-required {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    text-align: center;
    color: #6b7280;
    background: transparent;
    border-radius: 12px;
    margin: 20px;
}

.login-required-icon {
    font-size: 4rem;
    margin-bottom: 16px;
}

.login-required h2 {
    font-size: 1.5rem;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
}

.login-required p {
    font-size: 1rem;
    color: #6b7280;
    margin-bottom: 24px;
    max-width: 400px;
}

.login-required-btn {
    padding: 12px 32px;
    background: #2563eb;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.login-required-btn:hover {
    background: #1d4ed8;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
}

.login-required-btn:active {
    transform: translateY(0);
}
</style>