<template>
    <div class="scroll-wrapper">
        <div class="feedback">
            <div class="title-row">
                <img src="/images/libraryCard.jpg" alt="借閱證圖片" />
                <h1>意見信箱</h1>
            </div>
            <h1 class="section-title"> {{
                step === 1 ? '使用說明' :
                    step === 2 ? '讀者意見' :
                        '感謝您'
            }}</h1>

            <!-- 步驟一：使用聲明 -->
            <div v-if="step === 1" class="instructions">
                <ol>
                    <li>親愛的讀者，您好~😃</li>
                    <li>感謝您使用本館意見信箱留言，針對您的寶貴建議，我們會儘快回覆處理情形和進度，謝謝您的意見！</li>
                    <li>儘量以一件mail反映一件事情，以利案件處理，並簡潔、明確說明。</li>
                    <li>案件確定成立後，原則上辦理期限為七個工作天(不含週六日 及 例假日)。</li>
                    <li>請您於留言時提供正確之E-MAIL帳號、電話、真實姓名，俾系統自動回覆您信件收件編號及回覆內容亦將以E-MAIL的方式傳送給您。</li>
                </ol>

                <FormsConsentCheckbox v-model="agreed" />

                <ButtonsStartForm :disabled="!agreed" @next="step = 2">
                    前往意見留言
                </ButtonsStartForm>
            </div>

            <!-- ✅ 步驟二：申請表單 -->

            <form v-if="step === 2" @submit.prevent="submitForm" class="form">
                <div class="form-group">
                    <label class="form-label">姓名：</label>
                    <input v-model="form.name" required />
                </div>

                <div class="form-group">
                    <label class="form-label">借閱證號：<br>(非必填)</br></label>
                    <input v-model="form.cardNumber" />
                </div>

                <div class="form-group">
                    <label class="form-label">聯絡電話：</label>
                    <input v-model="form.phone" type="tel" required />
                </div>

                <div class="form-group">
                    <label class="form-label">電子郵件：</label>
                    <input v-model="form.email" type="email" required />
                </div>

                <div class="form-group">
                    <label class="form-label">主旨：</label>
                    <input v-model="form.subject" required />
                </div>

                <div class="form-group textarea-wrapper">
                    <label class="form-label">內容：</label>
                    <div class="textarea-container">
                        <textarea v-model="form.content" required rows="6" maxlength="1000"></textarea>
                        <span class="word-counter">{{ form.content.length }}/1000</span>
                    </div>
                </div>


                <div class="form-group captcha">
                    <label>驗證碼：</label>
                    <div class="captcha-row">
                        <img :src="captchaUrl" alt="驗證碼" class="captcha-img" />
                        <button type="button" @click="refreshCaptcha">↻</button>
                        <input v-model="form.captcha" required />
                    </div>
                </div>

                <div class="form-group-buttons">
                    <button type="button" @click="step = 1" class="back-button">← 回上一頁</button>
                    <button type="submit">確認送出</button>
                    <button type="button" @click="resetForm" class="reset-button">🔁 重新填寫</button>
                </div>
            </form>

            <!-- 步驟三：成功畫面 -->
            <div v-if="step === 3" class="success-step">
                <h2>✅ 意見送出成功！</h2>
                <p>感謝您的意見留言，本館將受理與了解，再回覆您的問題，謝謝!</p>
                <div v-if="loading" class="loading-spinner"></div>
                <p v-if="loading">即將返回首頁...</p>

                <ButtonsGoHome v-if="!loading" />
            </div>


        </div>

    </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import { watchEffect } from 'vue'
import { useStepReset } from '@/composables/useStepReset'

const loading = ref(false)

const step = ref(1)
const agreed = ref(false)

const form = reactive({
    name: '',
    cardNumber: '',
    phone: '',
    email: '',
    subject: '',
    content: '',
    captcha: ''
})

const captchaUrl = ref(getCaptchaUrl());

const route = useRoute()

watchEffect(() => {
    console.log('路由變化：', route.fullPath)
    if (route.query.reset === 'true') {
        step.value = 1
        resetForm()
    }
})


function getCaptchaUrl() {
    return `http://localhost:8080/api/captcha/m1?ts=${Date.now()}`; // 加上 timestamp 防止瀏覽器快取
}

function refreshCaptcha() {
    captchaUrl.value = getCaptchaUrl();
}

// 使用 useStepReset composable
useStepReset(step, resetForm)
function resetForm() {
    form.name = '';
    form.cardNumber = '';
    form.phone = '';
    form.email = '';
    form.subject = '';
    form.content = '';
    form.captcha = '';
    refreshCaptcha();
}

const submitted = ref(false)

const submitForm = async () => {
    try {
        await $fetch('http://localhost:8080/api/feedback', {
            method: 'POST',
            body: {
                name: form.name,
                cardNumber: form.cardNumber,
                phone: form.phone,
                email: form.email,
                subject: form.subject,
                content: form.content,
                captcha: form.captcha
            },
            credentials: 'include'
        });

        alert("✅ 送出成功！");
        submitted.value = true;
        step.value = 3;
    } catch (err) {
        // 回傳 400 會進來這裡
        const msg = err?.data || err?.message || '提交失敗';
        alert("❌ 錯誤：" + msg);
        refreshCaptcha(); // 驗證碼錯就重新載入
    }
}

</script>

<style scoped>
.loading-spinner {
    border: 6px solid #f3f3f3;
    border-top: 6px solid #003366;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin: 1rem auto;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.scroll-wrapper {
    position: relative;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.feedback {
    flex: 1;
    max-width: 1000px;
    /* max-height: 1000px; */
    margin: 0 auto;
    padding: 0 10px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    /* width: 100%; */
    height: 100%;
    scrollbar-width: thin;
    /* for Firefox */
    scrollbar-color: transparent transparent;
}


/* 滾動條預設為透明 */
.feedback::-webkit-scrollbar {
    width: 8px;
}

.feedback::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .feedback::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .feedback {
    scrollbar-color: rgba(0, 0, 0, 0.4) transparent;
}




.title-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    /* 圖片與文字間距 */
}

.title-row img {
    width: 50px;
    height: auto;
}

.title-row h1 {
    margin: 0;
    font-size: 2rem;
}

.section-title {
    position: relative;
    padding-left: 1rem;
    font-size: 1.5rem;
    font-weight: bold;
    padding: 0 1.5rem;
}

.section-title::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    background-color: skyblue;
    /* 橘色 */
    border-radius: 2px;
}

.section-title::after {
    content: "";
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    background-color: skyblue;
    border-radius: 2px;
}

.instructions li {
    margin-bottom: 0.5rem;
    /* 行與行之間的間距 */
    line-height: 2;
    /* 文字行高 */
    /* text-align: center; */
}

.instructions ul {
    padding-left: 1.5rem;
}

a {
    color: #007bff;
    text-decoration: underline;
}

a:hover {
    text-decoration: none;
}

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

.form {
    display: block;
    flex-direction: column;
    /* gap: 50px; */
    /* background-color: #0056b3; */
}


.form-group {
    display: flex;
    align-items: center;
    margin-bottom: 1.5rem;
    /* 控制每列之間的間距 */
    flex-wrap: wrap;
    /* 小螢幕時可換行 */
}

.form-group label {
    min-width: 120px;
    /* 統一 label 寬度，可依需求調整 */
    font-weight: bold;
    margin-right: 12px;
    text-align: right;
}

.form-group input,
.form-group select {
    flex: 1;
    padding: 8px;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 6px;
    min-width: 200px;
}

.form-group-buttons {
    display: flex;
    justify-content: center;
    gap: 1rem;
    margin-left: 5rem;
    margin: 0 auto 2rem;
}

.form-group.textarea-wrapper {
    display: flex;
    align-items: flex-start;
    margin-bottom: 1rem;
}

.form-group.textarea-wrapper .form-label {
    min-width: 80px;
    margin-right: 1rem;
    font-weight: bold;
    margin-top: 0.3rem;
    text-align: right;
    margin-left: 2.5rem;
}

.textarea-container {
    position: relative;
    flex: 1;

}

.textarea-container textarea {
    width: 100%;
    padding: 8px;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 6px;
    box-sizing: border-box;
    resize: vertical;
    min-height: 120px;
}

.word-counter {
    position: absolute;
    bottom: 6px;
    right: 10px;
    font-size: 0.8rem;
    color: #666;
    pointer-events: none;
}


.captcha-row {
    display: flex;
    align-items: center;
    gap: 8px;
}

.captcha-img {
    width: 120px;
    height: 40px;
    border: 1px solid #ccc;
    border-radius: 6px;
    /* ✅ 圓角圖片 */
    box-shadow: 0 0 4px rgba(0, 0, 0, 0.1);
}

.captcha-row button {
    background-color: lightgray;
    color: #000;
    border: none;
    border-radius: 6px;
    padding: 6px 8px;
    cursor: pointer;
    font-size: 20px;
    font-weight: bold;
    transition: background-color 0.3s;
}

.captcha-row button:hover {
    background-color: #2980b9;
}

.captcha-row input {
    height: 38px;
    padding: 6px 10px;
    border: 1px solid #ccc;
    border-radius: 6px;
    outline: none;
    font-size: 16px;
    width: 100px;
}


.form-label {
    width: 80px;
    font-weight: bold;
    margin-top: 0.3rem;
}


label {
    display: block;
    font-weight: bold;
    margin-bottom: 1rem;
    /* 控制垂直間距 */
}

input {
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 1rem;
}

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

.back-button {
    margin: 1rem;
    padding: 8px 14px;
    background-color: lightgray;
    border: 1px solid #999;
    border-radius: 6px;
    cursor: pointer;
}

.captcha-row {
    display: flex;
    align-items: center;
    gap: 8px;
    /* ✅ 按鈕與圖片、輸入欄位間距 */
}

.captcha-img {
    width: 120px;
    height: 40px;
    border: 1px solid #ccc;
    border-radius: 6px;
    /* ✅ 圓角圖片 */
    box-shadow: 0 0 4px rgba(0, 0, 0, 0.1);
}

.captcha-row button {
    background-color: lightgray;
    color: black;
    border: none;
    border-radius: 6px;
    padding: 6px 8px;
    cursor: pointer;
    font-size: 25px;
    font-weight: bold;
    transition: background-color 0.3s;
}

.captcha-row button:hover {
    background-color: #2980b9;
}

.captcha-row input {
    height: 38px;
    padding: 6px 10px;
    border: 1px solid #ccc;
    border-radius: 6px;
    outline: none;
    font-size: 16px;
    width: 100px;
}


.reset-button {
    margin: 1rem;
    padding: 8px 14px;
    background-color: lightgray;
    border: 1px solid #999;
    border-radius: 6px;
    cursor: pointer;
}

.success-step {
    text-align: center;
    padding: 40px 20px;
}

.success-step h2 {
    color: green;
    font-size: 2rem;
    margin-bottom: 1rem;
}

.success-step p {
    font-size: 1.2rem;
    margin-bottom: 2rem;
}

.success-step button {
    padding: 10px 20px;
    background-color: #2563eb;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 1rem;
}


.success {
    color: green;
    font-weight: bold;
}

.already-applied-step {
    background-color: #fff8e1;
    border: 1px solid #ffcc80;
    padding: 2rem;
    border-radius: 1rem;
    text-align: center;
    margin-top: 2rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.already-applied-step h2 {
    color: #e65100;
    font-size: 1.8rem;
    margin-bottom: 1rem;
}

.already-applied-step p {
    font-size: 1rem;
    color: #4e342e;
    margin-bottom: 1.5rem;
}

.already-applied-step button {
    background-color: #ff9800;
    color: white;
    border: none;
    padding: 0.6rem 1.2rem;
    font-size: 1rem;
    border-radius: 0.5rem;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.already-applied-step button:hover {
    background-color: #fb8c00;
}
</style>
