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
                    <li>親愛的讀者，您好~</li>
                    <li>感謝您使用本館意見信箱留言，針對您的寶貴建議，我們會儘快回覆處理情形和進度，謝謝您的意見！</li>
                    <li>儘量以一件mail反映一件事情，以利案件處理，並簡潔、明確說明。</li>
                    <li>案件確定成立後，原則上辦理期限為七個工作天(不含週六日 及 例假日)。</li>
                    <li>請您於留言時提供正確之E-MAIL帳號、電話、真實姓名，俾系統自動回覆您信件收件編號及回覆內容亦將以E-MAIL的方式傳送給您。</li>
                </ol>

                <label class="consent">
                    <input type="checkbox" v-model="agreed" />
                    我已閱讀並同意以上聲明
                </label>

                <button :disabled="!agreed" @click="step = 2" class="start-button">
                    前往意見留言
                </button>
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

                <div class="form-group">
                    <label class="form-label">內容：</label>
                    <textarea v-model="form.content" required rows="6" cols="50"></textarea>
                </div>

                <div class="form-group captcha">
                    <label>驗證碼：</label>
                    <div class="captcha-row">
                        <img :src="captchaUrl" alt="驗證碼" />
                        <button type="button" @click="refreshCaptcha">🔄</button>
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

                <button v-if="!loading" @click="delayedGoHome">回首頁</button>
                <!-- <button @click="delayedGoHome">回首頁</button> -->
            </div>

            <div v-if="step === 4" class="already-applied-step">
                <h2>⚠️ 您已申請過借閱證</h2>
                <p>系統判定您已辦理借閱證，請勿重複申請。如有疑問請洽客服。</p>
                <button @click="delayedGoHome">回首頁</button>
            </div>
        </div>

    </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useNavigation } from '@/composables/useNavigation'

const { goHome } = useNavigation()

const loading = ref(false)

const delayedGoHome = () => {
    loading.value = true
    setTimeout(() => {
        goHome()
    }, 3000)
}

const step = ref(1)
const agreed = ref(false)
const alreadyApplied = ref(false)

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

function getCaptchaUrl() {
    return `http://localhost:8080/api/captcha?ts=${Date.now()}`; // 防止快取
}

function refreshCaptcha() {
    captchaUrl.value = getCaptchaUrl();
}

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
    // 模擬已有申請過的身分證號
    if (form.idNumber === 'A123456789') {
        alreadyApplied.value = true
        step.value = 4 // 顯示「已申請過」畫面
        return
    }

    // ✅ 送出表單流程
    console.log('送出資料：', form)
    submitted.value = true
    step.value = 3
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

.consent {
    display: block;
    margin: 1rem auto;
    font-weight: bold;
    text-align: center;
    width: fit-content;
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


.education-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 1.5rem;
}

.form-label {
    width: 80px;
    font-weight: bold;
    margin-top: 0.3rem;
}

.gender-radio {
    display: flex;
    /* gap: 2rem; */
    min-width: 100px;
    font-weight: bold
}

.education-options {
    display: grid;
    grid-template-columns: repeat(3, minmax(200px, 1fr));
    /* 三欄排版 */
    gap: 1rem 2rem;
}

.education-options label {
    display: flex;
    align-items: center;
    text-align: center;
    gap: 6px;
}

.address-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8PX;
    margin-bottom: 0.5rem;
}

.address-row select,
.address-row input {
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font-size: 1rem;
    min-width: 140px;
}

.address-detail {
    display: flex;
    /* width: fit-content; */
    width: 100%;
    /* margin-left: 0; */
    padding-left: 130px;
    /* 避免被頂住，408 = 200 + 200 + 8 */
}

.address-detail input {
    width: 100%;
    max-width: calc(3 * 200px + 16px);
    /* 假設前面三個欄位每個200px，中間gap為8px*2 */
    padding: 8px;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 6px;
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
