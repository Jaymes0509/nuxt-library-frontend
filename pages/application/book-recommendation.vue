<template>
    <div class="scroll-wrapper">
        <LoginRequiredPrompt v-if="!isLoggedIn" message="您需要登入才能推薦書籍" />
        <div v-else class="feedback">
            <div class="title-row">
                <img src="/images/libraryCard.jpg" alt="借閱證圖片" />
                <h1>書籍薦購</h1>
            </div>
            <h1 class="section-title"> {{
                step === 1 ? '使用說明' :
                    step === 2 ? '推薦表單填寫' :
                        '表單送出成功'
            }}</h1>

            <!-- 步驟一：使用聲明 -->
            <div v-if="step === 1" class="instructions">
                <ol>
                    <li>親愛的讀者，您好~😃</li>
                    <li>感謝您使用本館書籍薦購功能，本功能僅限本館會員使用，每位會員最多推薦5筆（含圖書、視聽），請謹慎使用推薦額度。</li>
                    <li>手動輸入書名、ISBN欄位時請務必確認資料正確性。</li>
                    <li>每月薦購資料將於次月彙整後統一檢視</li>
                    <li>推薦好書前，請先確認該書是否已在本館館藏中。如已有該書，則不受理推薦。</li>
                    <li>審核納入採購清單的書籍，將依照採購時程購置入館，感謝您的耐心等候~</li>
                </ol>

                <FormsConsentCheckbox v-model="agreed" />

                <ButtonsStartForm :disabled="!agreed" @next="step = 2">
                    前往推薦
                </ButtonsStartForm>
            </div>

            <!-- ✅ 步驟二：填寫表單 -->
            <div v-if="step === 2">

                <!-- 推薦冊數顯示區塊 -->
                <div v-if="!loadingCount" class="recommend-count">
                    <div class="count-box">
                        <div class="title1">已推薦冊數</div>
                        <div class="value">{{ count.used }}</div>
                    </div>
                    <div class="count-box">
                        <div class="title2">尚可推薦冊數</div>
                        <div class="value">{{ count.remaining }}</div>
                    </div>

                </div>
                <div v-if="count.remaining === 0" class="form-block-message">
                    您已達推薦上限，無法再推薦更多書籍。
                </div>

                <form @submit.prevent="submitForm" class="form">
                    <div class="form-group">
                        <label class="form-label">書名：</label>
                        <input v-model="form.title" required />
                    </div>

                    <div class="form-group">
                        <label class="form-label">ISBN：</label>
                        <input v-model="form.isbn" required />
                    </div>

                    <div class="form-group">
                        <label class="form-label">作者：</label>
                        <input v-model="form.author" />
                    </div>

                    <div class="form-group">
                        <label class="form-label">出版社：</label>
                        <input v-model="form.publisher" />
                    </div>

                    <div class="form-group">
                        <label class="form-label">出版年：</label>
                        <input v-model="form.publishYear" type="number" />
                    </div>

                    <div class="form-group textarea-wrapper">
                        <label class="form-label-reason">推薦原因：</label>
                        <div class="textarea-container">
                            <textarea v-model="form.reason" required rows="6" maxlength="1000"></textarea>
                            <span class="word-counter">{{ form.reason.length }}/1000</span>
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
                        <ButtonsBackButton :step="step" @update:step="step = $event" />
                        <ButtonsSubmitButton>送出推薦</ButtonsSubmitButton>
                        <ButtonsResetButton @reset="resetForm" />
                    </div>
                </form>
            </div>

            <!-- 步驟三：成功畫面 -->
            <div v-if="step === 3" class="success-step">
                <h2>✅ 送出成功！</h2>
                <p>感謝您的推薦，本館將受理與審核，敬請耐心等候，謝謝!</p>
                <div v-if="loading" class="loading-spinner"></div>
                <p v-if="loading">即將返回首頁...</p>

                <ButtonsGoHome v-if="!loading" />
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStepReset } from '@/composables/useStepReset'


const step = ref(1)
const agreed = ref(false)
const loading = ref(false)

const form = reactive({
    title: '',
    isbn: '',
    author: '',
    publisher: '',
    publishYear: '',
    reason: '',
    captcha: ''
})

// 登入狀態檢查
const isLoggedIn = ref(false)
const count = reactive({ used: 0, remaining: 5 })
const loadingCount = ref(true)
const jwt = ref(localStorage.getItem("jwt_token"))
const captchaUrl = ref(getCaptchaUrl());

// 檢查登入狀態
const checkLoginStatus = () => {
    // 檢查 localStorage 中的用戶資訊
    const storedUser = localStorage.getItem('user')
    const jwtToken = localStorage.getItem('jwt_token')
    const authToken = localStorage.getItem('authToken')

    console.log('=== 登入狀態檢查 ===')
    console.log('storedUser:', storedUser)
    console.log('jwtToken:', jwtToken)
    console.log('authToken:', authToken)

    if (storedUser) {
        try {
            const user = JSON.parse(storedUser)
            isLoggedIn.value = true
            console.log('✅ 用戶已登入：', user)
        } catch (e) {
            console.error('❌ 解析用戶資訊失敗：', e)
            isLoggedIn.value = false
        }
    } else if (jwtToken || authToken) {
        // 如果有 token 但沒有用戶資訊，也視為已登入
        isLoggedIn.value = true
        console.log('✅ 檢測到登入 token')
    } else {
        isLoggedIn.value = false
        console.log('❌ 用戶未登入')
    }

    console.log('最終登入狀態：', isLoggedIn.value)
    console.log('==================')
}


const fetchCount = async () => {
    loadingCount.value = true
    const { data } = await useFetch('http://localhost:8080/api/recommendations/count', {
        headers: {
            Authorization: `Bearer ${jwt.value}`
        }
    })
    if (data.value) {
        count.used = data.value.used
        count.remaining = data.value.remaining
    }
    loadingCount.value = false
}

onMounted(() => {
    checkLoginStatus()
    fetchCount()
})

function getCaptchaUrl() {
    return `http://localhost:8080/api/captcha/m1?ts=${Date.now()}`; // 加上 timestamp 防止瀏覽器快取
}

function refreshCaptcha() {
    captchaUrl.value = getCaptchaUrl();
}

useStepReset(step, resetForm) // 使用 useStepReset composable

function resetForm() {
    form.title = '';
    form.isbn = '';
    form.author = '';
    form.publisher = '';
    form.publishYear = '';
    form.reason = '';
    form.captcha = '';
    refreshCaptcha();
}

const submitted = ref(false)

const submitForm = async () => {
    const jwt = ref(localStorage.getItem("jwt_token"))  // 或你實際存的 key 名稱
    if (!form.captcha) {
        alert("請輸入驗證碼");
        return;
    }

    if (count.remaining === 0) {
        alert("您已達推薦上限，無法再推薦");
        return;
    }

    try {
        await $fetch('http://localhost:8080/api/recommendations', {
            method: 'POST',
            body: {
                title: form.title,
                isbn: form.isbn,
                author: form.author,
                publisher: form.publisher,
                publishYear: form.publishYear ? Number(form.publishYear) : null,
                reason: form.reason,
                captcha: form.captcha
            },
            headers: {
                Authorization: `Bearer ${jwt.value}`
            },
            credentials: 'include'
        });

        await fetchCount(); //  更新推薦冊數
        alert("✅ 推薦成功！感謝您的建議");
        submitted.value = true;
        step.value = 3;

    } catch (err) {
        // 回傳 400 會進來這裡
        console.error(err) // 你可以看到錯誤的詳細內容
        const msg = err?.data?.message || err?.message || '提交失敗';
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
.scroll-wrapper:hover .feedbackd::-webkit-scrollbar-thumb {
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

.recommend-count {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    justify-content: center;
    margin-top: 20px;
}

.count-box {
    background-color: skyblue;
    color: white;
    margin-bottom: 1rem;
    padding: 10px 10px;
    border-radius: 10px;
    width: 300px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    text-align: center;
}

.count-box .title1 {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
    background-color: green;
}

.count-box .title2 {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
    background-color: red;
}

.count-box .value {
    background-color: #f0f0f0;
    color: #000;
    font-size: 36px;
    font-weight: bold;
    padding: 5px 0;
}

.form-block-message {
    margin-top: 20px;
    background-color: #ffe5e5;
    border: 1px solid #ff9999;
    color: #cc0000;
    padding: 15px;
    border-radius: 6px;
    text-align: center;
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
</style>
