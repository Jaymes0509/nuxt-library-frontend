<template>
    <div class="scroll-wrapper">
        <div class="library-card">
            <div class="title-row">
                <img src="/images/libraryCard.jpg" alt="借閱證圖片" />
                <h1>借閱證申請</h1>
            </div>
            <h1 class="section-title">申辦說明</h1>

            <!-- 步驟一：申請說明 + 同意聲明 -->
            <div v-if="step === 1" class="instructions">
                <ol>
                    <li>申辦對象：未曾申辦過本館個人借閱證之中華民國國民及持有居留證之非本國籍讀者</li>
                    <li>使用範圍：借閱證可借用本館內館藏資源(圖書、視聽資料、館內設備等)，並能使用本館所有線上資源。</li>
                    <li>領取：網路申請辦證後，請於本館開放時間內攜帶身分證明文件至櫃台領取。
                        <ul>
                            <li>中華民國國民應持個人身分證、駕照或戶口名簿(未滿十二歲之兒童由家長或法定監護人檢具身分證正本及戶口名簿正本代為辦理)。</li>
                            <li>非本國籍人士持護照或居留證。</li>
                        </ul>
                    </li>
                    <li>借閱說明：借閱證每人限辦乙張，個人借閱證可借閱圖書30冊、期刊5冊、視聽資料5件，圖書、期刊借期30天，可續借兩次。漫畫及視聽資料借期14天，不得續借。</li>
                    <li>遺失補發：
                        <ul>
                            <li>掛失登記：借閱證如遺失時，應立即向本館辦理掛失登記，若因未掛失或轉借而發生冒用情事，應自行負責相關賠償之責。</li>
                            <li>遺失補發: 檢具相關身分證明文件，須繳交行政規費新臺幣參拾元整(繳付工本費30元整)，申請補發</li>
                            <li>讀者資料如有變更，應即向本館辦理異動手續。</li>
                        </ul>
                    </li>
                </ol>

                <label class="consent">
                    <input type="checkbox" v-model="agreed" />
                    我已閱讀並同意以上聲明
                </label>

                <button :disabled="!agreed" @click="step = 2" class="start-button">
                    開始申請網路辦證
                </button>
            </div>

            <!-- ✅ 步驟二：申請表單 -->
            <form v-else @submit.prevent="submitForm" class="form">
                <label>姓名：<input v-model="form.name" required /></label>
                <label>電子郵件：<input v-model="form.email" type="email" required /></label>
                <label>身分證字號：<input v-model="form.idNumber" required /></label>
                <label>出生日期：<input v-model="form.birthDate" type="date" required /></label>
                <label>聯絡電話：<input v-model="form.phone" type="tel" required /></label>

                <button type="button" @click="step = 1" class="back-button">← 回上一頁</button>

                <button type="submit">送出申請</button>
                <p v-if="submitted" class="success">✅ 申請成功！我們將盡快與您聯絡。</p>
            </form>
        </div>

    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const step = ref(1)
const agreed = ref(false)

const form = reactive({
    name: '',
    email: '',
    idNumber: '',
    birthDate: '',
    phone: ''
})

const submitted = ref(false)

const submitForm = () => {
    console.log('送出資料：', form)
    submitted.value = true
}
</script>

<style scoped>
.scroll-wrapper {
    position: relative;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.library-card {
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
.library-card::-webkit-scrollbar {
    width: 8px;
}

.library-card::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .library-card::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .library-card {
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
    display: flex;
    flex-direction: column;
    gap: 12px;
}

label {
    display: flex;
    flex-direction: column;
    font-weight: bold;
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

.success {
    color: green;
    font-weight: bold;
}
</style>
